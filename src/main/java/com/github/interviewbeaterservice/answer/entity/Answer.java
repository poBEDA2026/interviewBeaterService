package com.github.interviewbeaterservice.answer.entity;

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

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = "text")
@Table(name = "answers")
public class Answer {

  /**
   * Конструктор для {@link Builder}.
   *
   * @param text      текст ответа
   * @param isCorrect флаг «ответ верный»
   */
  @Builder
  Answer(String text, boolean isCorrect) {
    this.text = text;
    this.isCorrect = isCorrect;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(nullable = false, length = 500)
  private String text;

  @Column(length = 1000)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(name = "is_correct", nullable = false)
  private boolean isCorrect;
}
