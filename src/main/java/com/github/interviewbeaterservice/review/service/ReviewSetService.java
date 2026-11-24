package com.github.interviewbeaterservice.review.service;

import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import com.github.interviewbeaterservice.review.dto.ReviewSetRequest;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.entity.ReviewSetItem;
import com.github.interviewbeaterservice.review.exception.ReviewSetNotFoundException;
import com.github.interviewbeaterservice.review.exception.ReviewSetValidationException;
import com.github.interviewbeaterservice.review.repository.ReviewSetItemRepository;
import com.github.interviewbeaterservice.review.repository.ReviewSetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления подборками вопросов для ревью: создание, чтение
 * и получение списка подборок.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewSetService {

  private final ReviewSetRepository reviewSetRepository;
  private final ReviewSetItemRepository reviewSetItemRepository;
  private final QuestionRepository questionRepository;

  /**
   * Создание новой подборки с указанными вопросами
   *
   * @param req запрос на создание подборки {@link ReviewSetRequest}
   * @return созданная подборка {@link ReviewSet}
   * @throws ReviewSetValidationException если список вопросов пуст или содержит дубликаты
   * @throws EntityNotFoundException если хотя бы один из указанных вопросов не найден
   */
  @Transactional
  public ReviewSet create(ReviewSetRequest req) {
    if (req.questionIds() == null || req.questionIds().isEmpty()) {
      throw new ReviewSetValidationException("questionIds must not be empty");
    }

    Set<Long> unique = new HashSet<>(req.questionIds());
    if (unique.size() != req.questionIds().size()) {
      throw new ReviewSetValidationException("questionIds must not contain duplicates");
    }

    List<Question> questions = questionRepository.findAllById(req.questionIds());
    if (questions.size() != req.questionIds().size()) {
      throw new EntityNotFoundException("One or more questions not found: " + req.questionIds());
    }

    ReviewSet set = ReviewSet.builder()
        .title(req.title())
        .description(req.description())
        .build();
    reviewSetRepository.save(set);

    for (int i = 0; i < questions.size(); i++) {
      Question question = questions.get(i);
      ReviewSetItem item = ReviewSetItem.builder()
          .set(set).question(question).position(i).build();
      reviewSetItemRepository.save(item);
    }

    return set;
  }

  /**
   * Получение подборки по идентификатору
   *
   * @param id идентификатор подборки
   * @return подборка {@link ReviewSet}
   * @throws ReviewSetNotFoundException если подборка с указанным идентификатором не найдена
   */
  public ReviewSet getById(Long id) {
    return reviewSetRepository.findById(id)
        .orElseThrow(() -> new ReviewSetNotFoundException(id));
  }

  /**
   * Получение списка всех подборок
   *
   * @return список подборок {@link ReviewSet}
   */
  public List<ReviewSet> listAll() {
    return reviewSetRepository.findAll();
  }
}