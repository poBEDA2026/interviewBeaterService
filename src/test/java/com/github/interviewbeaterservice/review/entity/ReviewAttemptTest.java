package com.github.interviewbeaterservice.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.interviewbeaterservice.user.entity.User;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты сущности {@link ReviewAttempt}: инициализация полей билдером
 * и простановка временны́х метрик JPA-колбэками.
 */
class ReviewAttemptTest {

  @Test
  @DisplayName("Билдер устанавливает статус IN_PROGRESS и пустой счётчик правильных ответов")
  void builderInitializesStatusInProgress() {
    User u = new User();
    ReviewSet s = ReviewSet.builder().title("X").build();
    ReviewAttempt a = ReviewAttempt.builder()
        .user(u).set(s).totalQuestions(3).build();

    assertThat(a.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
    assertThat(a.getCorrectCount()).isNull();
  }

  @Test
  @DisplayName("Колбэк @PrePersist проставляет время старта попытки")
  void prePersistSetsStartedAt() throws Exception {
    User u = new User();
    ReviewSet s = ReviewSet.builder().title("X").build();
    ReviewAttempt a = ReviewAttempt.builder()
        .user(u).set(s).totalQuestions(3).build();
    invoke(a, "onCreate");
    assertThat(get(a, "startedAt")).isNotNull();
  }

  private static void invoke(Object target, String name) throws Exception {
    Method m = target.getClass().getDeclaredMethod(name);
    m.setAccessible(true);
    m.invoke(target);
  }

  private static Object get(Object target, String name) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f.get(target);
  }
}