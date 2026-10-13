package com.github.interviewbeaterservice.review.dto;

import java.time.Instant;
import java.util.List;

/**
 * Ответ API с данными подборки ревью: идентификатор, заголовок, описание,
 * количество вопросов и упорядоченный список позиций {@link ReviewSetItemResponse}.
 *
 * @param id идентификатор подборки
 * @param title заголовок подборки
 * @param description описание подборки
 * @param questionCount общее число вопросов в подборке
 * @param createdAt время создания подборки
 * @param items упорядоченные по позиции элементы подборки
 */
public record ReviewSetResponse(
    Long id,
    String title,
    String description,
    int questionCount,
    Instant createdAt,
    List<ReviewSetItemResponse> items
) {

}
