package com.github.interviewbeaterservice.review.controller;

import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.answer.repository.AnswerRepository;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import com.github.interviewbeaterservice.review.dto.AnswerOptionResponse;
import com.github.interviewbeaterservice.review.dto.ReviewSetItemResponse;
import com.github.interviewbeaterservice.review.dto.ReviewSetRequest;
import com.github.interviewbeaterservice.review.dto.ReviewSetResponse;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.entity.ReviewSetItem;
import com.github.interviewbeaterservice.review.repository.ReviewSetItemRepository;
import com.github.interviewbeaterservice.review.service.ReviewSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления подборками вопросов ревью:
 * создание, получение списка и чтение содержимого подборки.
 */
@RestController
@RequestMapping("/review-sets")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Подборки вопросов для review")
public class ReviewSetController {

  private final ReviewSetService reviewSetService;
  private final ReviewSetItemRepository itemRepository;
  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;

  /**
   * Создание новой подборки вопросов
   *
   * @param req запрос на создание подборки {@link ReviewSetRequest}
   * @return созданная подборка {@link ReviewSetResponse}
   * @throws jakarta.validation.ConstraintViolationException если запрос не проходит валидацию
   */
  @Operation(summary = "Создать подборку")
  @PostMapping
  public ReviewSetResponse create(@Valid @RequestBody ReviewSetRequest req) {
    ReviewSet set = reviewSetService.create(req);
    return toResponse(set);
  }

  /**
   * Получение списка всех подборок
   *
   * @return список подборок {@link ReviewSetResponse}
   */
  @Operation(summary = "Список подборок")
  @GetMapping
  public List<ReviewSetResponse> list() {
    return reviewSetService.listAll().stream().map(this::toResponse).toList();
  }

  /**
   * Получение содержимого конкретной подборки
   *
   * @param id идентификатор подборки
   * @return подборка {@link ReviewSetResponse} с упорядоченным списком вопросов
   */
  @Operation(summary = "Содержимое подборки")
  @GetMapping("/{id}")
  public ReviewSetResponse get(@PathVariable Long id) {
    return toResponse(reviewSetService.getById(id));
  }

  private ReviewSetResponse toResponse(ReviewSet set) {
    List<ReviewSetItem> items = itemRepository.findAllBySetIdOrderByPositionAsc(set.getId());
    List<ReviewSetItemResponse> itemResponses = new ArrayList<>(items.size());

    for (ReviewSetItem item : items) {
      Question question = questionRepository.findById(item.getQuestion().getId()).orElseThrow();
      List<Answer> answers = answerRepository.findAllByQuestionId(question.getId());
      List<AnswerOptionResponse> opts = answers.stream()
          .map(a -> new AnswerOptionResponse(a.getId(), a.getText()))
          .toList();
      itemResponses.add(new ReviewSetItemResponse(item.getPosition(), question.getId(),
          question.getTitle() == null ? "" : question.getTitle(), opts));
    }

    return new ReviewSetResponse(
        set.getId(), set.getTitle(), set.getDescription(),
        items.size(), set.getCreatedAt(), itemResponses);
  }
}