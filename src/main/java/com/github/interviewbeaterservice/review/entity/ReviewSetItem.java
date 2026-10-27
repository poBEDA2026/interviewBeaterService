package com.github.interviewbeaterservice.review.entity;

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
 * Позиция вопроса внутри подборки {@link ReviewSet}: связывает вопрос
 * {@link Question} с подборкой и определяет его порядковый номер.
 */
@Entity
@Table(name = "review_set_items")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"set", "question"})
public class ReviewSetItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "set_id", nullable = false)
  private ReviewSet set;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(nullable = false)
  private int position;

  /**
   * Создание позиции вопроса внутри подборки
   *
   * @param set подборка {@link ReviewSet}, в которую входит позиция
   * @param question вопрос {@link Question}, включаемый в подборку
   * @param position порядковая позиция вопроса в подборке
   */
  @Builder
  public ReviewSetItem(ReviewSet set, Question question, int position) {
    this.set = set;
    this.question = question;
    this.position = position;
  }
}