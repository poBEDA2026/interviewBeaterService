package com.github.interviewbeaterservice.review.dto;

import java.util.List;

/**
 * Состояние одного вопроса внутри попытки ревью: позиция, заголовок,
 * варианты ответов и выбранный пользователем вариант (если есть).
 *
 * @param questionId идентификатор вопроса
 * @param position позиция вопроса внутри подборки
 * @param title заголовок вопроса
 * @param answers варианты ответа на вопрос
 * @param selectedAnswerId идентификатор выбранного пользователем варианта, либо null
 */
public record AttemptQuestionResponse(
    Long questionId,
    int position,
    String title,
    List<AnswerOptionResponse> answers,
    Long selectedAnswerId
) {

}
