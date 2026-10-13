package com.github.interviewbeaterservice.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.interviewbeaterservice.question.entity.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты сущности {@link ReviewSetItem}: проверка присваивания полей билдером.
 */
class ReviewSetItemTest {

  @Test
  @DisplayName("Билдер сохраняет подборку, вопрос и позицию")
  void builderAssignsFields() {
    ReviewSet set = ReviewSet.builder().title("X").build();
    Question q = new Question();

    ReviewSetItem item = ReviewSetItem.builder()
        .set(set).question(q).position(0).build();

    assertThat(item.getSet()).isSameAs(set);
    assertThat(item.getQuestion()).isSameAs(q);
    assertThat(item.getPosition()).isZero();
  }
}