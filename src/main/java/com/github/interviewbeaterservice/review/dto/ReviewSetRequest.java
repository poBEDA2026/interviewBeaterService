package com.github.interviewbeaterservice.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Тело запроса на создание подборки ревью: заголовок, описание
 * и список идентификаторов вопросов, включаемых в подборку.
 *
 * @param title заголовок подборки
 * @param description описание подборки
 * @param questionIds идентификаторы вопросов, включаемых в подборку
 */
public record ReviewSetRequest(
    @NotBlank String title,
    String description,
    @NotEmpty List<Long> questionIds
) {

}
