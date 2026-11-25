package com.github.interviewbeaterservice.question.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title"})
@Table(name = "questions")
public class Question {

    @Builder
    Question(String title, Category category, Difficulty difficulty) {
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;

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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "difficulty_id", nullable = false)
    private Difficulty difficulty;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Не сериализуем в JSON: отдаётся отдельным эндпоинтом /questions/{id}/attachments,
    // а ленивая загрузка при сериализации роняла GET /questions (no session).
    @JsonIgnore
    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) createdAt = now;

        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }
}
