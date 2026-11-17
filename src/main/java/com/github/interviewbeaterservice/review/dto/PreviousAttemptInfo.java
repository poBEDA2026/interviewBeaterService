package com.github.interviewbeaterservice.review.dto;

import java.time.Instant;

/**
 * Краткая информация о предыдущей попытке ревью для сравнения с текущей.
 *
 * @param attemptId идентификатор предыдущей попытки
 * @param correctCount количество правильных ответов в предыдущей попытке
 * @param percentage процент правильных ответов в предыдущей попытке
 * @param submittedAt время сабмита предыдущей попытки
 */
public record PreviousAttemptInfo(
    Long attemptId,
    int correctCount,
    double percentage,
    Instant submittedAt
) {

}
