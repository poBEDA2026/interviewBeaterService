package com.github.interviewbeaterservice.review.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
 * Подборка вопросов для повторного прохождения (review) пользователями.
 * Хранит заголовок, описание и упорядоченный список позиций {@link ReviewSetItem}.
 */
@Entity
@Table(name = "review_sets")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "items")
public class ReviewSet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Setter(AccessLevel.NONE)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  @Setter(AccessLevel.NONE)
  private Instant updatedAt;

  @OneToMany(mappedBy = "set", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ReviewSetItem> items = new ArrayList<>();

  /**
   * Создание новой подборки ревью с заданными заголовком и описанием
   *
   * @param title заголовок подборки
   * @param description описание подборки
   */
  @Builder
  public ReviewSet(String title, String description) {
    this.title = title;
    this.description = description;
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}