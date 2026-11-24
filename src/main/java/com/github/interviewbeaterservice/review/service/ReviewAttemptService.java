package com.github.interviewbeaterservice.review.service;

import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.answer.repository.AnswerRepository;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import com.github.interviewbeaterservice.review.entity.ReviewAnswer;
import com.github.interviewbeaterservice.review.entity.ReviewAttempt;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.entity.ReviewSetItem;
import com.github.interviewbeaterservice.review.exception.InvalidReviewAnswerException;
import com.github.interviewbeaterservice.review.exception.ReviewAttemptAlreadySubmittedException;
import com.github.interviewbeaterservice.review.exception.ReviewAttemptNotFoundException;
import com.github.interviewbeaterservice.review.repository.ReviewAnswerRepository;
import com.github.interviewbeaterservice.review.repository.ReviewAttemptRepository;
import com.github.interviewbeaterservice.review.repository.ReviewSetItemRepository;
import com.github.interviewbeaterservice.review.util.PercentUtil;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления попытками прохождения подборок: старт, сохранение
 * ответов и сабмит с подсчётом правильных ответов.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewAttemptService {

  private final ReviewAttemptRepository attemptRepository;
  private final ReviewSetItemRepository itemRepository;
  private final ReviewAnswerRepository answerRepository;
  private final UserRepository userRepository;
  private final ReviewSetService reviewSetService;
  private final AnswerRepository coreAnswerRepository;

  /**
   * Старт новой попытки либо возврат уже существующей незавершённой
   *
   * @param userId идентификатор пользователя
   * @param setId идентификатор подборки
   * @return попытка {@link ReviewAttempt} в статусе {@link AttemptStatus#IN_PROGRESS}
   * @throws EntityNotFoundException если пользователь или подборка не найдены
   */
  @Transactional
  public ReviewAttempt startAttempt(Long userId, Long setId) {
    Optional<ReviewAttempt> existing = attemptRepository
        .findFirstByUserIdAndSetIdAndStatus(userId, setId, AttemptStatus.IN_PROGRESS);

    if (existing.isPresent()) {
      return existing.get();
    }

    ReviewSet set = reviewSetService.getById(setId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User " + userId + " not found"));
    ReviewAttempt attempt = ReviewAttempt.builder()
        .user(user).set(set)
        .totalQuestions(set.getItems().size())
        .build();

    return attemptRepository.save(attempt);
  }

  /**
   * Сохранение либо замена ответа на конкретный вопрос внутри попытки
   *
   * @param userId идентификатор пользователя
   * @param attemptId идентификатор попытки
   * @param questionId идентификатор вопроса
   * @param answerId идентификатор выбранного варианта ответа
   * @return сохранённый ответ {@link ReviewAnswer}
   * @throws ReviewAttemptNotFoundException если попытка не найдена или принадлежит другому пользователю
   * @throws ReviewAttemptAlreadySubmittedException если попытка уже сабмитнута
   * @throws InvalidReviewAnswerException если вопрос не входит в подборку или вариант ответа не принадлежит вопросу, либо ответ не найден
   */
  @Transactional
  public ReviewAnswer saveAnswer(Long userId, Long attemptId, Long questionId, Long answerId) {
    ReviewAttempt attempt = attemptRepository.findById(attemptId)
        .orElseThrow(() -> new ReviewAttemptNotFoundException(attemptId));

    if (!attempt.getUser().getId().equals(userId)) {
      throw new ReviewAttemptNotFoundException(attemptId);
    }

    if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
      throw new ReviewAttemptAlreadySubmittedException(attemptId);
    }

    List<ReviewSetItem> items = itemRepository.findAllBySetIdOrderByPositionAsc(
        attempt.getSet().getId());

    Question question = items.stream()
        .map(ReviewSetItem::getQuestion)
        .filter(q -> q.getId().equals(questionId))
        .findFirst()
        .orElseThrow(() -> new InvalidReviewAnswerException(
            "Question " + questionId + " is not in this set"));

    Answer answer = coreAnswerRepository.findById(answerId)
        .orElseThrow(() -> new InvalidReviewAnswerException("Answer " + answerId + " not found"));

    if (!answer.getQuestion().getId().equals(questionId)) {
      throw new InvalidReviewAnswerException(
          "Answer " + answerId + " does not belong to question " + questionId);
    }

    Optional<ReviewAnswer> existing = answerRepository.findByAttemptIdAndQuestionId(attemptId,
        questionId);

    if (existing.isPresent()) {
      ReviewAnswer ra = existing.get();
      ra.setAnswer(answer);
      ra.setIsCorrect(null);
      return answerRepository.save(ra);
    }

    ReviewAnswer ra = ReviewAnswer.builder()
        .attempt(attempt).question(question).answer(answer).build();

    return answerRepository.save(ra);
  }

  /**
   * Результат сабмита попытки: сабмитнутая попытка, предыдущая сабмитнутая
   * попытка пользователя по той же подборке (если есть), количество
   * правильных ответов и процент
   *
   * @param attempt сабмитнутая попытка с проставленным статусом и временем завершения
   * @param previous предыдущая сабмитнутая попытка по той же подборке, если она была
   * @param correctCount количество правильных ответов
   * @param percentage процент правильных ответов от общего числа вопросов
   */
  public record SubmitResult(ReviewAttempt attempt, Optional<ReviewAttempt> previous,
                             int correctCount, double percentage) {

  }

  /**
   * Сабмит попытки: подсчёт правильных ответов и фиксация результата
   *
   * @param userId идентификатор пользователя
   * @param attemptId идентификатор попытки
   * @return результат {@link SubmitResult}
   * @throws ReviewAttemptNotFoundException если попытка не найдена или принадлежит другому пользователю
   * @throws IllegalStateException если у вопроса нет правильного ответа (невозможно создать skip-sentinel)
   */
  @Transactional
  public SubmitResult submit(Long userId, Long attemptId) {
    ReviewAttempt attempt = attemptRepository.findById(attemptId)
        .orElseThrow(() -> new ReviewAttemptNotFoundException(attemptId));

    if (!attempt.getUser().getId().equals(userId)) {
      throw new ReviewAttemptNotFoundException(attemptId);
    }

    if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
      Optional<ReviewAttempt> previous = attemptRepository
          .findFirstByUserIdAndSetIdAndStatusAndSubmittedAtBeforeOrderBySubmittedAtDesc(
              userId, attempt.getSet().getId(), AttemptStatus.SUBMITTED, attempt.getSubmittedAt());

      return new SubmitResult(attempt, previous,
          attempt.getCorrectCount() == null ? 0 : attempt.getCorrectCount(),
          PercentUtil.percentage(attempt.getCorrectCount() == null ? 0 : attempt.getCorrectCount(),
              attempt.getTotalQuestions()));
    }

    List<ReviewSetItem> items = itemRepository.findAllBySetIdOrderByPositionAsc(
        attempt.getSet().getId());
    List<ReviewAnswer> saved = answerRepository.findAllByAttemptId(attemptId);
    Map<Long, ReviewAnswer> byQuestion = saved.stream()
        .collect(Collectors.toMap(ra -> ra.getQuestion().getId(), ra -> ra));

    int correct = 0;
    for (ReviewSetItem item : items) {
      Long qId = item.getQuestion().getId();
      ReviewAnswer ra = byQuestion.get(qId);

      if (ra == null) {
        Answer sentinel = coreAnswerRepository.findFirstByQuestionIdAndIsCorrectTrue(qId)
            .orElseThrow(() -> new IllegalStateException(
                "Question " + qId + " has no correct answer for skip-row sentinel"));

        ReviewAnswer skip = ReviewAnswer.builder()
            .attempt(attempt).question(item.getQuestion()).answer(sentinel)
            .build();

        skip.setIsCorrect(false);
        answerRepository.save(skip);

        continue;
      }

      boolean isCorrect = ra.getAnswer().isCorrect();
      ra.setIsCorrect(isCorrect);
        if (isCorrect) {
            correct++;
        }

      answerRepository.save(ra);
    }

    attempt.setStatus(AttemptStatus.SUBMITTED);
    attempt.setSubmittedAt(Instant.now());
    attempt.setCorrectCount(correct);
    attemptRepository.save(attempt);

    Optional<ReviewAttempt> previous = attemptRepository
        .findFirstByUserIdAndSetIdAndStatusAndSubmittedAtBeforeOrderBySubmittedAtDesc(
            userId, attempt.getSet().getId(), AttemptStatus.SUBMITTED, attempt.getSubmittedAt());

    return new SubmitResult(attempt, previous, correct,
        PercentUtil.percentage(correct, attempt.getTotalQuestions()));
  }

  /**
   * Получение текущего состояния попытки (без проверки статуса)
   *
   * @param userId идентификатор пользователя
   * @param attemptId идентификатор попытки
   * @return попытка {@link ReviewAttempt}
   * @throws ReviewAttemptNotFoundException если попытка не найдена или принадлежит другому пользователю
   */
  public ReviewAttempt getState(Long userId, Long attemptId) {
    ReviewAttempt a = attemptRepository.findById(attemptId)
        .orElseThrow(() -> new ReviewAttemptNotFoundException(attemptId));

    if (!a.getUser().getId().equals(userId)) {
      throw new ReviewAttemptNotFoundException(attemptId);
    }

    return a;
  }

  /**
   * Получение сабмитнутой попытки для просмотра результата
   *
   * @param userId идентификатор пользователя
   * @param attemptId идентификатор попытки
   * @return сабмитнутая попытка {@link ReviewAttempt}
   * @throws ReviewAttemptNotFoundException если попытка не найдена или принадлежит другому пользователю
   * @throws IllegalStateException если попытка ещё не сабмитнута
   */
  public ReviewAttempt getResult(Long userId, Long attemptId) {
    ReviewAttempt a = getState(userId, attemptId);

    if (a.getStatus() != AttemptStatus.SUBMITTED) {
      throw new IllegalStateException("Attempt " + attemptId + " is not submitted yet");
    }

    return a;
  }
}