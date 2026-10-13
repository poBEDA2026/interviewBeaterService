package com.github.interviewbeaterservice.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты сущности {@link ReviewSet}: простановка и обновление временны́х
 * метрик JPA-колбэками {@code @PrePersist} и {@code @PreUpdate}.
 */
class ReviewSetTest {

  @Test
  @DisplayName("Колбэк @PrePersist проставляет время создания и обновления")
  void prePersistSetsTimestamps() throws Exception {
    ReviewSet set = ReviewSet.builder().title("Java").build();
    invoke(set, "onCreate");
    assertThat(get(set, "createdAt")).isNotNull();
    assertThat(get(set, "updatedAt")).isNotNull();
  }

  @Test
  @DisplayName("Колбэк @PreUpdate обновляет время последнего изменения")
  void preUpdateRefreshesUpdatedAt() throws Exception {
    ReviewSet set = ReviewSet.builder().title("Java").build();
    invoke(set, "onCreate");
    Instant first = (Instant) get(set, "updatedAt");
    Thread.sleep(10);
    invoke(set, "onUpdate");
    Instant second = (Instant) get(set, "updatedAt");
    assertThat(second).isAfter(first);
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