package com.github.interviewbeaterservice.review.entity;

import com.github.interviewbeaterservice.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Попытка прохождения подборки {@link ReviewSet} конкретным пользователем {@link User}:
 * хранит статус, временные метки старта/сабмита и набор ответов {@link ReviewAnswer}.
 */
@Entity
@Table(name = "review_attempts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"user", "set", "answers"})
public class ReviewAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "set_id", nullable = false)
  private ReviewSet set;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AttemptStatus status;

  @Column(name = "started_at", nullable = false, updatable = false)
  @Setter(AccessLevel.NONE)
  private Instant startedAt;

  @Column(name = "submitted_at")
  private Instant submittedAt;

  @Column(name = "total_questions", nullable = false)
  private int totalQuestions;

  @Column(name = "correct_count")
  private Integer correctCount;

  @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ReviewAnswer> answers = new ArrayList<>();

  /**
   * Создание новой попытки прохождения подборки в статусе IN_PROGRESS
   *
   * @param user пользователь {@link User}, проходящий попытку
   * @param set подборка {@link ReviewSet}, которая проходится
   * @param totalQuestions общее число вопросов в подборке
   */
  @Builder
  public ReviewAttempt(User user, ReviewSet set, int totalQuestions) {
    this.user = user;
    this.set = set;
    this.totalQuestions = totalQuestions;
    this.status = AttemptStatus.IN_PROGRESS;
  }

  @PrePersist
  void onCreate() {
    this.startedAt = Instant.now();
  }
}