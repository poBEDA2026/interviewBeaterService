package com.github.interviewbeaterservice.answer.dto;

import com.github.interviewbeaterservice.answer.entity.Answer;

/**
 * Плоский DTO для отдачи ответа наружу
 * Не содержит JPA-entity, поэтому ответ можно безопасно класть в Redis-кэш
 *
 * @param id          идентификатор ответа
 * @param questionId  идентификатор вопроса, которому принадлежит ответ
 * @param text        текст ответа
 * @param description пояснение к ответу
 * @param correct     признак правильности ответа
 */
public record AnswerResponse(
        Long id,
        Long questionId,
        String text,
        String description,
        boolean correct
) {

    public static AnswerResponse from(Answer answer) {
        return new AnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getText(),
                answer.getDescription(),
                answer.isCorrect()
        );
    }
}
