package com.github.interviewbeaterservice.statistics.entity;

import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "correctCount", "wrongCount"})
@Table(name = "user_question_stats")
public class UserQuestionStats {

    @Builder
    UserQuestionStats(User user, Question question, Long correctCount, Long wrongCount, Instant lastAnsweredAt) {
        this.user = user;
        this.question = question;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.lastAnsweredAt = lastAnsweredAt;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "correct_count", nullable = false)
    private Long correctCount;

    @Column(name = "wrong_count", nullable = false)
    private Long wrongCount;

    @Column(name = "last_answered_at", nullable = false)
    private Instant lastAnsweredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
