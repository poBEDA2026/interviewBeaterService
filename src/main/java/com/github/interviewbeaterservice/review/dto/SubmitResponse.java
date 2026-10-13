package com.github.interviewbeaterservice.review.dto;

import java.time.Instant;

/**
 * Ответ API при сабмите попытки ревью: результат текущей попытки,
 * информация о предыдущей попытке и оценка прогресса.
 *
 * @param attemptId идентификатор текущей попытки
 * @param setId идентификатор подборки
 * @param setTitle заголовок подборки
 * @param totalQuestions общее число вопросов в подборке
 * @param correctCount количество правильных ответов в текущей попытке
 * @param percentage процент правильных ответов в текущей попытке
 * @param submittedAt время сабмита текущей попытки
 * @param previousAttempt информация о предыдущей попытке, если она была
 * @param improvement сравнение текущей попытки с предыдущей
 */
public record SubmitResponse(
    Long attemptId,
    Long setId,
    String setTitle,
    int totalQuestions,
    int correctCount,
    double percentage,
    Instant submittedAt,
    PreviousAttemptInfo previousAttempt,
    ImprovementInfo improvement
) {

}
