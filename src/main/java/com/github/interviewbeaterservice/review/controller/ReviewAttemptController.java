package com.github.interviewbeaterservice.review.controller;

import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.answer.repository.AnswerRepository;
import com.github.interviewbeaterservice.auth.AuthContext;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import com.github.interviewbeaterservice.review.dto.AnswerOptionResponse;
import com.github.interviewbeaterservice.review.dto.AttemptQuestionResponse;
import com.github.interviewbeaterservice.review.dto.AttemptStateResponse;
import com.github.interviewbeaterservice.review.dto.ImprovementInfo;
import com.github.interviewbeaterservice.review.dto.PreviousAttemptInfo;
import com.github.interviewbeaterservice.review.dto.SaveAnswerRequest;
import com.github.interviewbeaterservice.review.dto.StartAttemptResponse;
import com.github.interviewbeaterservice.review.dto.SubmitResponse;
import com.github.interviewbeaterservice.review.entity.ReviewAnswer;
import com.github.interviewbeaterservice.review.entity.ReviewAttempt;
import com.github.interviewbeaterservice.review.entity.ReviewSetItem;
import com.github.interviewbeaterservice.review.repository.ReviewAnswerRepository;
import com.github.interviewbeaterservice.review.repository.ReviewSetItemRepository;
import com.github.interviewbeaterservice.review.service.ReviewAttemptService;
import com.github.interviewbeaterservice.review.util.PercentUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST-контроллер для управления попытками прохождения подборок:
 * старт, сохранение ответов, сабмит и чтение состояния/результата.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Review", description = "Попытки прохождения подборок")
public class ReviewAttemptController {

  private final ReviewAttemptService attemptService;
  private final ReviewSetItemRepository itemRepository;
  private final ReviewAnswerRepository answerRepository;
  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository2;

  /**
   * Старт попытки прохождения подборки (idempotent — при наличии незавершённой
   * попытки возвращается она)
   *
   * @param setId идентификатор подборки
   * @return данные стартованной попытки {@link StartAttemptResponse}
   */
  @Operation(summary = "Стартовать попытку (idempotent)")
  @PostMapping("/review-sets/{setId}/attempts")
  public StartAttemptResponse start(@PathVariable Long setId) {
    Long userId = currentUserId();
    ReviewAttempt a = attemptService.startAttempt(userId, setId);

    return new StartAttemptResponse(a.getId(), a.getSet().getId(), a.getSet().getTitle(),
        a.getStatus(), a.getStartedAt(), a.getTotalQuestions());
  }

  /**
   * Получение текущего состояния попытки вместе с уже сохранёнными ответами
   *
   * @param id идентификатор попытки
   * @return состояние попытки {@link AttemptStateResponse}
   */
  @Operation(summary = "Получить состояние попытки")
  @GetMapping("/attempts/{id}")
  public AttemptStateResponse state(@PathVariable Long id) {
    Long userId = currentUserId();
    ReviewAttempt a = attemptService.getState(userId, id);

    return buildState(a);
  }

  /**
   * Сохранение (или замена) ответа внутри попытки без сабмита
   *
   * @param id идентификатор попытки
   * @param req запрос {@link SaveAnswerRequest} с идентификаторами вопроса и варианта ответа
   * @return мапа с идентификаторами сохранённого ответа и вопроса
   */
  @Operation(summary = "Сохранить ответ (без submit)")
  @PostMapping("/attempts/{id}/answers")
  public Map<String, Object> saveAnswer(@PathVariable Long id,
      @Valid @RequestBody SaveAnswerRequest req) {
    Long userId = currentUserId();
    ReviewAnswer ra = attemptService.saveAnswer(userId, id, req.questionId(), req.answerId());

    return Map.of("answerId", ra.getId(), "questionId", ra.getQuestion().getId());
  }

  /**
   * Сабмит попытки: подсчёт правильных ответов и фиксация результата
   *
   * @param id идентификатор попытки
   * @return результат сабмита {@link SubmitResponse}
   */
  @Operation(summary = "Сабмит попытки")
  @PostMapping("/attempts/{id}/submit")
  public SubmitResponse submit(@PathVariable Long id) {
    Long userId = currentUserId();
    ReviewAttemptService.SubmitResult r = attemptService.submit(userId, id);

    return toSubmitResponse(r);
  }

  /**
   * Получение результата ранее сабмитнутой попытки
   *
   * @param id идентификатор попытки
   * @return результат попытки {@link SubmitResponse}
   */
  @Operation(summary = "Получить результат сабмитнутой попытки")
  @GetMapping("/attempts/{id}/result")
  public SubmitResponse result(@PathVariable Long id) {
    Long userId = currentUserId();
    ReviewAttempt a = attemptService.getResult(userId, id);

    return toSubmitResponse(new ReviewAttemptService.SubmitResult(
        a, Optional.empty(), a.getCorrectCount(),
        PercentUtil.percentage(a.getCorrectCount(), a.getTotalQuestions())));
  }

  private Long currentUserId() {
    return AuthContext.currentUserId()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
  }

  private AttemptStateResponse buildState(ReviewAttempt attempt) {
    List<ReviewSetItem> items = itemRepository.findAllBySetIdOrderByPositionAsc(attempt.getSet().getId());
    List<ReviewAnswer> saved = answerRepository.findAllByAttemptId(attempt.getId());
    List<AttemptQuestionResponse> questions = new ArrayList<>();

    for (ReviewSetItem item : items) {
      Question question = questionRepository.findById(item.getQuestion().getId()).orElseThrow();
      List<Answer> answers = answerRepository2.findAllByQuestionId(question.getId());

      List<AnswerOptionResponse> opts = answers.stream()
          .map(an -> new AnswerOptionResponse(an.getId(), an.getText())).toList();

      Long selected = saved.stream()
          .filter(r -> r.getQuestion().getId().equals(question.getId()))
          .map(r -> r.getAnswer().getId())
          .findFirst().orElse(null);

      questions.add(new AttemptQuestionResponse(question.getId(), item.getPosition(),
          question.getTitle() == null ? "" : question.getTitle(), opts, selected));
    }

    return new AttemptStateResponse(attempt.getId(), attempt.getSet().getId(), attempt.getStatus(),
        attempt.getTotalQuestions(), attempt.getStartedAt(), questions);
  }

  private SubmitResponse toSubmitResponse(ReviewAttemptService.SubmitResult review) {
    ReviewAttempt attempt = review.attempt();

    PreviousAttemptInfo prev = review.previous().map(p -> new PreviousAttemptInfo(
        p.getId(), p.getCorrectCount(),
        PercentUtil.percentage(p.getCorrectCount(), p.getTotalQuestions()), p.getSubmittedAt()))
        .orElse(null);

    ImprovementInfo imp = review.previous().map(p -> new ImprovementInfo(
        attempt.getCorrectCount() - p.getCorrectCount(),
        PercentUtil.round(
            review.percentage() - PercentUtil.percentage(p.getCorrectCount(), p.getTotalQuestions()))
    )).orElse(null);

    return new SubmitResponse(attempt.getId(), attempt.getSet().getId(), attempt.getSet().getTitle(),
        attempt.getTotalQuestions(), attempt.getCorrectCount(), review.percentage(),
        attempt.getSubmittedAt(), prev, imp);
  }
}