package com.github.interviewbeaterservice.review.dto;

import java.util.List;

/**
 * Элемент подборки ревью: позиция, идентификатор и заголовок вопроса,
 * а также варианты ответов с их текстом.
 *
 * @param position позиция вопроса внутри подборки
 * @param questionId идентификатор вопроса
 * @param questionTitle заголовок вопроса
 * @param answers варианты ответа на вопрос
 */
public record ReviewSetItemResponse(
    int position,
    Long questionId,
    String questionTitle,
    List<AnswerOptionResponse> answers
) {

}
