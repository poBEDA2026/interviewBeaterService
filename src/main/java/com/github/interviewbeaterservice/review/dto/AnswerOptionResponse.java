package com.github.interviewbeaterservice.review.dto;

/**
 * Ответ-вариант внутри вопроса попытки ревью: идентификатор и текст варианта.
 *
 * @param id идентификатор варианта ответа
 * @param text текст варианта ответа
 */
public record AnswerOptionResponse(Long id, String text) {

}
