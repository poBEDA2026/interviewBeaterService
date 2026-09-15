package com.github.interviewbeaterservice.statistics.dto;

import java.time.Instant;

public record QuestionStatsResponse(
        Long questionId,
        String questionTitle,
        Long correctCount,
        Long wrongCount,
        Instant lastAnsweredAt
) {
}
