package com.github.interviewbeaterservice.question.exception;

public class AttachmentNotFoundException extends RuntimeException {

    public AttachmentNotFoundException(Long questionId, Long attachmentId) {
        super("Attachment " + attachmentId + " for question " + questionId + " not found");
    }
}
