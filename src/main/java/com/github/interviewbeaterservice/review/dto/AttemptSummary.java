package com.github.interviewbeaterservice.review.dto;

import java.time.Instant;

/**
 * Краткая сводка по сабмитнутой попытке ревью: количество правильных
 * ответов, процент и время сабмита.
 *
 * @param attemptId идентификатор попытки
 * @param correctCount количество правильных ответов
 * @param percentage процент правильных ответов от общего числа вопросов
 * @param submittedAt время сабмита попытки
 */
public record AttemptSummary(
    Long attemptId,
    int correctCount,
    double percentage,
    Instant submittedAt
) {

}
