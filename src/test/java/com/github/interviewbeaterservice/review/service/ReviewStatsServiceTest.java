package com.github.interviewbeaterservice.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import com.github.interviewbeaterservice.review.dto.ReviewHistoryItemResponse;
import com.github.interviewbeaterservice.review.dto.SetStatsResponse;
import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import com.github.interviewbeaterservice.review.entity.ReviewAttempt;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.exception.ReviewSetNotFoundException;
import com.github.interviewbeaterservice.review.repository.ReviewAttemptRepository;
import com.github.interviewbeaterservice.user.entity.User;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Юнит-тесты сервиса {@link ReviewStatsService}: группировка истории попыток по подборкам
 * и расчёт агрегатов (последняя, лучшая попытка, средний процент, прирост прогресса).
 */
@ExtendWith(MockitoExtension.class)
class ReviewStatsServiceTest {

  @Mock
  ReviewAttemptRepository attemptRepository;
  @Mock
  ReviewSetService reviewSetService;

  ReviewStatsService service;

  @BeforeEach
  void setUp() {
    service = new ReviewStatsService(attemptRepository, reviewSetService);
  }

  @Test
  @DisplayName("История попыток схлопывается до последней попытки по каждой подборке")
  void historyGroupsBySetTakingLatest() {
    ReviewSet s1 = newSet(1L, "Java");
    ReviewSet s2 = newSet(2L, "SQL");
    ReviewAttempt a1 = submitted(10L, 1L, s1, 3, 2, Instant.now().minusSeconds(200));
    ReviewAttempt a2 = submitted(11L, 1L, s1, 3, 3, Instant.now().minusSeconds(100));
    ReviewAttempt a3 = submitted(20L, 2L, s2, 2, 1, Instant.now().minusSeconds(50));
    // Ruling: brief's stub List.of(a2,a3,a1) is NOT desc-by-submittedAt;
    // service trusts repo order (contract: findAll...OrderBySubmittedAtDesc).
    // Stub returns truly-desc order so the test exercises the "latest per set" contract.
    when(attemptRepository.findAllByUserIdAndStatusOrderBySubmittedAtDesc(99L,
        AttemptStatus.SUBMITTED))
        .thenReturn(List.of(a3, a2, a1));

    List<ReviewHistoryItemResponse> history = service.history(99L);

    assertThat(history).hasSize(2);
    assertThat(history.get(0).setId()).isEqualTo(2L); // самый поздний
    assertThat(history.get(1).setId()).isEqualTo(1L);
    assertThat(history.get(1).lastCorrectCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Статистика по подборке считает количество попыток, лучший результат, средний процент и прирост прогресса")
  void statsForSetComputesAggregates() {
    ReviewSet s = newSet(1L, "Java");
    when(reviewSetService.getById(1L)).thenReturn(s);
    ReviewAttempt first = submitted(10L, 1L, s, 5, 1, Instant.parse("2026-01-01T00:00:00Z"));
    ReviewAttempt mid = submitted(11L, 1L, s, 5, 3, Instant.parse("2026-02-01T00:00:00Z"));
    ReviewAttempt last = submitted(12L, 1L, s, 5, 4, Instant.parse("2026-03-01T00:00:00Z"));
    when(attemptRepository.findAllByUserIdAndSetIdAndStatusOrderBySubmittedAtAsc(99L, 1L,
        AttemptStatus.SUBMITTED))
        .thenReturn(List.of(first, mid, last));

    SetStatsResponse stats = service.statsForSet(99L, 1L);

    assertThat(stats.attemptsCount()).isEqualTo(3);
    assertThat(stats.lastAttempt().correctCount()).isEqualTo(4);
    assertThat(stats.bestAttempt().correctCount()).isEqualTo(4);
    assertThat(stats.averagePercentage()).isCloseTo(53.33, within(0.01)); // (20+60+80)/3
    assertThat(stats.progressDelta()).isEqualTo(60.0); // 80-20
  }

  @Test
  @DisplayName("Статистика по подборке без завершённых попыток выбрасывает IllegalStateException")
  void statsForSetEmptyHistoryThrows() {
    ReviewSet s = newSet(1L, "Java");
    when(reviewSetService.getById(1L)).thenReturn(s);
    when(attemptRepository.findAllByUserIdAndSetIdAndStatusOrderBySubmittedAtAsc(99L, 1L,
        AttemptStatus.SUBMITTED))
        .thenReturn(List.of());
    assertThatThrownBy(() -> service.statsForSet(99L, 1L))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Статистика по несуществующей подборке пробрасывает ReviewSetNotFoundException")
  void statsForSetMissingSetThrows404() {
    when(reviewSetService.getById(99L)).thenThrow(new ReviewSetNotFoundException(99L));
    assertThatThrownBy(() -> service.statsForSet(99L, 99L))
        .isInstanceOf(ReviewSetNotFoundException.class);
  }

  private static ReviewSet newSet(Long id, String title) {
    ReviewSet s = ReviewSet.builder().title(title).build();
    setId(s, id);
    return s;
  }

  private static ReviewAttempt submitted(Long id, Long userId, ReviewSet set, int total,
      int correct, Instant at) {
    User u = User.builder().email("u@x").password("p").build();
    setId(u, userId);
    ReviewAttempt a = ReviewAttempt.builder().user(u).set(set).totalQuestions(total).build();
    setId(a, id);
    a.setStatus(AttemptStatus.SUBMITTED);
    a.setCorrectCount(correct);
    a.setSubmittedAt(at);
    return a;
  }

  private static void setId(Object target, Long id) {
    try {
      Field f = target.getClass().getDeclaredField("id");
      f.setAccessible(true);
      f.set(target, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
