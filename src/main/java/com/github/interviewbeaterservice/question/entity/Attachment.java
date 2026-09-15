package com.github.interviewbeaterservice.question.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Вложение вопроса: метаданные файла, размещённого в S3-хранилище.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "originalName"})
@Table(name = "attachments")
public class Attachment {

    /**
     * Конструктор для {@link Builder}: заполняет контентные поля вложения.
     */
    @Builder
    Attachment(String storageKey, String originalName, String contentType, long sizeBytes) {
        this.storageKey = storageKey;
        this.originalName = originalName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "storage_key", nullable = false, length = 512, unique = true)
    private String storageKey;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "content_type", nullable = false, length = 127)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    /**
     * Проставить дату загрузки перед первым сохранением, если она не задана.
     */
    @PrePersist
    private void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
    }
}
