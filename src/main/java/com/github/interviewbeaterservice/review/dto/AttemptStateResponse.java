package com.github.interviewbeaterservice.review.dto;

import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import java.time.Instant;
import java.util.List;

/**
 * Полное состояние попытки ревью: метаданные, статус и список вопросов
 * с уже выбранными ответами.
 *
 * @param attemptId идентификатор попытки
 * @param setId идентификатор подборки
 * @param status текущий статус попытки
 * @param totalQuestions общее число вопросов в подборке
 * @param startedAt время старта попытки
 * @param questions вопросы подборки с выбранными ответами
 */
public record AttemptStateResponse(
    Long attemptId,
    Long setId,
    AttemptStatus status,
    int totalQuestions,
    Instant startedAt,
    List<AttemptQuestionResponse> questions
) {

}
