package com.github.interviewbeaterservice.review.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Тело запроса на сохранение ответа внутри попытки ревью: идентификатор
 * вопроса и выбранный вариант ответа.
 *
 * @param questionId идентификатор вопроса
 * @param answerId идентификатор выбранного варианта ответа
 */
public record SaveAnswerRequest(
    @NotNull Long questionId,
    @NotNull Long answerId
) {

}
