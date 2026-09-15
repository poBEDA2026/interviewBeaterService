package com.github.interviewbeaterservice.question.exception;

/**
 * Вложение не прошло валидацию (пустое, слишком большое, недопустимый Content-Type и т.п.).
 */
public class InvalidAttachmentException extends RuntimeException {

    /**
     * @param message текст ошибки валидации
     */
    public InvalidAttachmentException(String message) {
        super(message);
    }
}
