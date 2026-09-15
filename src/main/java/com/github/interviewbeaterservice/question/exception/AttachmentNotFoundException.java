package com.github.interviewbeaterservice.question.exception;

/**
 * Вложение не найдено у указанного вопроса.
 */
public class AttachmentNotFoundException extends RuntimeException {

    /**
     * @param questionId   ID вопроса
     * @param attachmentId ID вложения
     */
    public AttachmentNotFoundException(Long questionId, Long attachmentId) {
        super("Attachment %d for question %d not found".formatted(attachmentId, questionId));
    }
}
