package com.github.interviewbeaterservice.review.util;

/**
 * Утилита расчёта процентного соотношения и округления до двух знаков после запятой.
 * <p>
 * Используется review-модулем для расчёта процента правильных ответов и округления
 * дельт/средних значений статистики. Формулы соответствуют проектной конвенции
 * (factor-based {@code Math.round}, без {@code BigDecimal}/{@code RoundingMode}).
 */
public final class PercentUtil {

  private PercentUtil() {
  }

  /**
   * Процент правильных ответов, округлённый до двух знаков после запятой.
   *
   * @param correct количество правильных ответов
   * @param total   общее количество вопросов; {@code 0} возвращает {@code 0.0}
   * @return процент в диапазоне {@code [0.0, 100.0]}, округлённый до двух знаков
   */
  public static double percentage(int correct, int total) {
    if (total == 0) {
      return 0.0;
    }
    return Math.round(correct * 10000.0 / total) / 100.0;
  }

  /**
   * Округление произвольного значения до двух знаков после запятой.
   *
   * @param value исходное значение
   * @return значение, округлённое до двух знаков после запятой
   */
  public static double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}
