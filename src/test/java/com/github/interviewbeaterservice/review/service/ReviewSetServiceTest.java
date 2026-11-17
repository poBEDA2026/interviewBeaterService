package com.github.interviewbeaterservice.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import com.github.interviewbeaterservice.review.dto.ReviewSetRequest;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.exception.ReviewSetNotFoundException;
import com.github.interviewbeaterservice.review.exception.ReviewSetValidationException;
import com.github.interviewbeaterservice.review.repository.ReviewSetItemRepository;
import com.github.interviewbeaterservice.review.repository.ReviewSetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Юнит-тесты сервиса {@link ReviewSetService}: валидация входных данных при создании
 * подборки и обработка отсутствующей подборки при чтении.
 */
@ExtendWith(MockitoExtension.class)
class ReviewSetServiceTest {

  @Mock
  ReviewSetRepository reviewSetRepository;
  @Mock
  ReviewSetItemRepository reviewSetItemRepository;
  @Mock
  QuestionRepository questionRepository;

  ReviewSetService service;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    service = new ReviewSetService(reviewSetRepository, reviewSetItemRepository,
        questionRepository);
  }

  @Test
  @DisplayName("Создание подборки сохраняет саму подборку и по элементу на каждый вопрос")
  void createSavesSetAndItems() {
    ReviewSetRequest req = new ReviewSetRequest("Title", "desc", List.of(1L, 2L));
    Question q1 = new Question();
    setId(q1, 1L);
    Question q2 = new Question();
    setId(q2, 2L);
    when(questionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(q1, q2));
    when(reviewSetRepository.save(any(ReviewSet.class))).thenAnswer(inv -> {
      ReviewSet s = inv.getArgument(0);
      setId(s, 100L);
      return s;
    });

    ReviewSet result = service.create(req);

    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getTitle()).isEqualTo("Title");
    verify(reviewSetItemRepository, times(2)).save(
        any(com.github.interviewbeaterservice.review.entity.ReviewSetItem.class));
  }

  @Test
  @DisplayName("Создание подборки с пустым списком вопросов отклоняется без обращения к репозиториям")
  void createRejectsEmptyQuestionIds() {
    ReviewSetRequest req = new ReviewSetRequest("Title", "desc", List.of());
    assertThatThrownBy(() -> service.create(req)).isInstanceOf(ReviewSetValidationException.class);
    verifyNoInteractions(reviewSetRepository, reviewSetItemRepository, questionRepository);
  }

  @Test
  @DisplayName("Создание подборки с повторяющимися идентификаторами вопросов отклоняется")
  void createRejectsDuplicateQuestionIds() {
    ReviewSetRequest req = new ReviewSetRequest("Title", "desc", List.of(1L, 1L));
    assertThatThrownBy(() -> service.create(req)).isInstanceOf(ReviewSetValidationException.class);
  }

  @Test
  @DisplayName("Создание подборки падает, если один из вопросов не найден")
  void createThrowsWhenQuestionMissing() {
    ReviewSetRequest req = new ReviewSetRequest("Title", "desc", List.of(1L, 2L));
    Question q1 = new Question();
    setId(q1, 1L);
    when(questionRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(q1)); // only one

    assertThatThrownBy(() -> service.create(req)).isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("Чтение несуществующей подборки выбрасывает ReviewSetNotFoundException")
  void getByIdThrowsWhenMissing() {
    when(reviewSetRepository.findById(7L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.getById(7L)).isInstanceOf(ReviewSetNotFoundException.class);
  }

  private static void setId(Object target, Long id) {
    try {
      var f = target.getClass().getDeclaredField("id");
      f.setAccessible(true);
      f.set(target, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
