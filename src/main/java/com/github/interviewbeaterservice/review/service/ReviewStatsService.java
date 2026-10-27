package com.github.interviewbeaterservice.review.service;

import com.github.interviewbeaterservice.review.dto.AttemptSummary;
import com.github.interviewbeaterservice.review.dto.ReviewHistoryItemResponse;
import com.github.interviewbeaterservice.review.dto.SetStatsResponse;
import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import com.github.interviewbeaterservice.review.entity.ReviewAttempt;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.exception.ReviewSetNotFoundException;
import com.github.interviewbeaterservice.review.repository.ReviewAttemptRepository;
import com.github.interviewbeaterservice.review.util.PercentUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис агрегированной статистики по ревью: история последних попыток
 * пользователя и сводные показатели по конкретной подборке.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewStatsService {

  private final ReviewAttemptRepository attemptRepository;
  private final ReviewSetService reviewSetService;

  /**
   * Получение истории ревью пользователя: по одной последней сабмитнутой
   * попытке на каждую подборку
   *
   * @param userId идентификатор пользователя
   * @return список {@link ReviewHistoryItemResponse}, упорядоченный по убыванию времени сабмита
   */
  public List<ReviewHistoryItemResponse> history(Long userId) {
    List<ReviewAttempt> all = attemptRepository
        .findAllByUserIdAndStatusOrderBySubmittedAtDesc(userId, AttemptStatus.SUBMITTED);

    Map<Long, ReviewAttempt> latestBySet = new LinkedHashMap<>();
    for (ReviewAttempt a : all) {
      latestBySet.putIfAbsent(a.getSet().getId(), a);
    }

    List<ReviewHistoryItemResponse> out = new ArrayList<>();
    for (ReviewAttempt a : latestBySet.values()) {
      out.add(new ReviewHistoryItemResponse(
          a.getSet().getId(),
          a.getSet().getTitle(),
          a.getId(),
          a.getCorrectCount(),
          PercentUtil.percentage(a.getCorrectCount(), a.getTotalQuestions()),
          a.getSubmittedAt()));
    }

    return out;
  }

  /**
   * Получение агрегированной статистики по подборке: число попыток,
   * последняя и лучшая попытки, средний процент и дельта прогресса
   *
   * @param userId идентификатор пользователя
   * @param setId идентификатор подборки
   * @return статистика {@link SetStatsResponse}
   * @throws ReviewSetNotFoundException если подборка не найдена
   * @throws IllegalStateException если у пользователя нет сабмитнутых попыток по этой подборке
   */
  public SetStatsResponse statsForSet(Long userId, Long setId) {
    ReviewSet set = reviewSetService.getById(setId);

    List<ReviewAttempt> all = attemptRepository
        .findAllByUserIdAndSetIdAndStatusOrderBySubmittedAtAsc(userId, setId,
            AttemptStatus.SUBMITTED);

    if (all.isEmpty()) {
      throw new IllegalStateException("No submitted attempts for set " + setId);
    }

    ReviewAttempt last = all.get(all.size() - 1);
    ReviewAttempt first = all.get(0);
    ReviewAttempt best = all.stream().max(Comparator.comparingInt(ReviewAttempt::getCorrectCount))
        .orElseThrow();

    double avg = all.stream().mapToInt(ReviewAttempt::getCorrectCount).average().orElse(0);
    double avgPct = Math.round(avg * 100.0 / first.getTotalQuestions() * 100) / 100.0;
    double progress = PercentUtil.round(
        PercentUtil.percentage(last.getCorrectCount(), last.getTotalQuestions())
            - PercentUtil.percentage(first.getCorrectCount(), first.getTotalQuestions()));

    return new SetStatsResponse(
        set.getId(), set.getTitle(), all.size(),
        toSummary(last), toSummary(best), avgPct, progress);
  }

  private static AttemptSummary toSummary(ReviewAttempt a) {
    return new AttemptSummary(a.getId(), a.getCorrectCount(),
        PercentUtil.percentage(a.getCorrectCount(), a.getTotalQuestions()), a.getSubmittedAt());
  }
}