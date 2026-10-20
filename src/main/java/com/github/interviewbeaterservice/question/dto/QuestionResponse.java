package com.github.interviewbeaterservice.question.dto;

import com.github.interviewbeaterservice.question.entity.Question;

import java.time.Instant;

/**
 * Плоский DTO для отдачи вопроса наружу
 * Не содержит JPA-entity, поэтому вопрос можно безопасно класть в Redis-кэш
 *
 * @param id        идентификатор вопроса
 * @param title     заголовок вопроса
 * @param createdAt момент создания
 */
public record QuestionResponse(Long id, String title, Instant createdAt) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), question.getTitle(), question.getCreatedAt());
    }
}