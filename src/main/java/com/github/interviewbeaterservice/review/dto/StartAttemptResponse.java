package com.github.interviewbeaterservice.review.dto;

import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import java.time.Instant;

/**
 * Ответ API при старте попытки ревью: идентификаторы попытки и подборки,
 * заголовок подборки, статус, время старта и общее число вопросов.
 *
 * @param attemptId идентификатор попытки
 * @param setId идентификатор подборки
 * @param setTitle заголовок подборки
 * @param status текущий статус попытки
 * @param startedAt время старта попытки
 * @param totalQuestions общее число вопросов в подборке
 */
public record StartAttemptResponse(
    Long attemptId,
    Long setId,
    String setTitle,
    AttemptStatus status,
    Instant startedAt,
    int totalQuestions
) {

}
