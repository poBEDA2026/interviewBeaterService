package com.github.interviewbeaterservice.question.exception;

/**
 * Вопрос не найден.
 */
public class QuestionNotFoundException extends RuntimeException {

    /**
     * @param id ID вопроса
     */
    public QuestionNotFoundException(Long id) {
        super("Question " + id + " not found");
    }
}
