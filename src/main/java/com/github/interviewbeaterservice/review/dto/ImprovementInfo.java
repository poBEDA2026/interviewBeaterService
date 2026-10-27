package com.github.interviewbeaterservice.review.dto;

/**
 * Сравнение текущей попытки с предыдущей: абсолютная и процентная дельта результата.
 *
 * @param absoluteDelta разница в количестве правильных ответов
 * @param percentageDelta разница в процентах правильных ответов
 */
public record ImprovementInfo(int absoluteDelta, double percentageDelta) {

}
