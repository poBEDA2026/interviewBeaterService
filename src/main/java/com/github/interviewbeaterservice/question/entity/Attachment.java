package com.github.interviewbeaterservice.question.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
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
@ToString(of = {"id", "originalName"})
@Table(name = "attachments")
public class Attachment {

  /**
   * Конструктор для {@link Builder}.
   *
   * @param storageKey   storage key объекта в S3
   * @param originalName исходное имя файла
   * @param contentType  MIME-тип файла
   * @param sizeBytes    размер файла в байтах
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
   * Проставляет {@code uploadedAt} при первом сохранении сущности.
   */
  @PrePersist
  private void onCreate() {
    if (uploadedAt == null) {
      uploadedAt = Instant.now();
    }
  }
}
