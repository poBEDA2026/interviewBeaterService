package com.github.interviewbeaterservice.question.dto;

import com.github.interviewbeaterservice.question.entity.Attachment;

import java.time.Instant;

public record AttachmentResponse(
        Long id,
        String originalName,
        String contentType,
        long sizeBytes,
        Instant uploadedAt
) {
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
