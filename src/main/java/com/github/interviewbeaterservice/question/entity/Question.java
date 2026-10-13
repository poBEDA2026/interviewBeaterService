package com.github.interviewbeaterservice.question.entity;

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

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title"})
@Table(name = "questions")
public class Question {

  @Builder
  Question(String title) {
    this.title = title;

    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(nullable = false, length = 500)
  private String title;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "question", fetch = FetchType.LAZY,
      cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attachment> attachments = new ArrayList<>();

  @PrePersist
  private void onCreate() {
    Instant now = Instant.now();

      if (createdAt == null) {
          createdAt = now;
      }

      if (updatedAt == null) {
          updatedAt = now;
      }
  }

  @PreUpdate
  private void onUpdate() {
    updatedAt = Instant.now();
  }
}
