package com.github.interviewbeaterservice.review.entity;

import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.question.entity.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Ответ пользователя на конкретный вопрос {@link Question} внутри попытки
 * {@link ReviewAttempt}: хранит выбранный вариант {@link Answer} и признак корректности.
 */
@Entity
@Table(name = "review_answers")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"attempt", "question", "answer"})
public class ReviewAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "attempt_id", nullable = false)
  private ReviewAttempt attempt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "answer_id", nullable = false)
  private Answer answer;

  @Column(name = "is_correct")
  private Boolean isCorrect;

  /**
   * Создание ответа внутри попытки на конкретный вопрос с выбранным вариантом
   *
   * @param attempt попытка {@link ReviewAttempt}, к которой относится ответ
   * @param question вопрос {@link Question}, на который дан ответ
   * @param answer выбранный вариант ответа {@link Answer}
   */
  @Builder
  public ReviewAnswer(ReviewAttempt attempt, Question question, Answer answer) {
    this.attempt = attempt;
    this.question = question;
    this.answer = answer;
  }
}