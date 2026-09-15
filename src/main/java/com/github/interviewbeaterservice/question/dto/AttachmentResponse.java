package com.github.interviewbeaterservice.question.dto;

import com.github.interviewbeaterservice.question.entity.Attachment;

import java.time.Instant;

/**
 * DTO вложения вопроса в ответах API.
 */
public record AttachmentResponse(
        Long id,
        String originalName,
        String contentType,
        long sizeBytes,
        Instant uploadedAt
) {
    /**
     * Собрать DTO из сущности вложения.
     *
     * @param a сущность вложения
     * @return DTO с метаданными вложения
     */
    public static AttachmentResponse from(Attachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getOriginalName(),
                a.getContentType(),
                a.getSizeBytes(),
                a.getUploadedAt()
        );
    }
}
