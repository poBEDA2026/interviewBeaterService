package com.github.interviewbeaterservice.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты сущности {@link ReviewAnswer}: проверка поведения билдера.
 */
class ReviewAnswerTest {

  @Test
  @DisplayName("Билдер оставляет признак корректности неопределённым")
  void builderLeavesIsCorrectNull() {
    ReviewAttempt attempt = ReviewAttempt.builder()
        .user(new User()).set(ReviewSet.builder().title("X").build())
        .totalQuestions(1).build();
    Question q = new Question();
    Answer a = new Answer();

    ReviewAnswer ra = ReviewAnswer.builder()
        .attempt(attempt).question(q).answer(a).build();

    assertThat(ra.getIsCorrect()).isNull();
  }
}