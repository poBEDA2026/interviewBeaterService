package com.github.interviewbeaterservice.review.dto;

import java.time.Instant;

/**
 * Элемент истории ревью пользователя: последняя попытка по конкретной подборке,
 * использованный для отображения в общем списке.
 *
 * @param setId идентификатор подборки
 * @param setTitle заголовок подборки
 * @param lastAttemptId идентификатор последней попытки по подборке
 * @param lastCorrectCount количество правильных ответов в последней попытке
 * @param lastPercentage процент правильных ответов в последней попытке
 * @param lastSubmittedAt время сабмита последней попытки
 */
public record ReviewHistoryItemResponse(
    Long setId,
    String setTitle,
    Long lastAttemptId,
    int lastCorrectCount,
    double lastPercentage,
    Instant lastSubmittedAt
) {

}
