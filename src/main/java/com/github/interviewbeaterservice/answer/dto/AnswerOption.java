package com.github.interviewbeaterservice.answer.dto;

/**
 * Вариант ответа на вопрос, доступный клиенту до отправки ответа.
 * Не содержит признак правильности, чтобы его нельзя было подсмотреть.
 *
 * @param id   идентификатор варианта
 * @param text текст варианта
 */
public record AnswerOption(Long id, String text) {
}
