package com.github.interviewbeaterservice.answer.dto;

public record AnswerSubmissionResponse(
        Boolean correct,
        Long selectedAnswerId,
        Long correctAnswerId,
        String description
) {
}
