package com.github.interviewbeaterservice.review.dto;

/**
 * Статистика по подборке ревью: последняя и лучшая попытки, средний процент
 * и дельта прогресса между первой и последней попытками.
 *
 * @param setId идентификатор подборки
 * @param setTitle заголовок подборки
 * @param attemptsCount общее число сабмитнутых попыток по подборке
 * @param lastAttempt сводка по последней попытке
 * @param bestAttempt сводка по лучшей попытке
 * @param averagePercentage средний процент правильных ответов по всем попыткам
 * @param progressDelta разница в процентах между последней и первой попытками
 */
public record SetStatsResponse(
    Long setId,
    String setTitle,
    long attemptsCount,
    AttemptSummary lastAttempt,
    AttemptSummary bestAttempt,
    double averagePercentage,
    double progressDelta
) {

}
