package com.github.interviewbeaterservice.statistics.service;

import com.github.interviewbeaterservice.statistics.entity.UserQuestionStats;
import com.github.interviewbeaterservice.statistics.repository.UserQuestionStatsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final UserQuestionStatsRepository statsRepository;

    /**
     * Получить всю статистику пользователя
     * @param userId ID пользователя
     * @return список записей статистики
     */
    public List<UserQuestionStats> getForUser(Long userId) {
        return statsRepository.findAllByUserId(userId);
    }

    /**
     * Получить статистику пользователя по конкретному вопросу
     * @param userId     ID пользователя
     * @param questionId ID вопроса
     * @return запись статистики
     * @throws EntityNotFoundException если статистики по этой паре ещё нет
     */
    public UserQuestionStats getForUserAndQuestion(Long userId, Long questionId) {
        return statsRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseThrow(() -> new EntityNotFoundException("Statistics not found"));
    }

    /**
     * Зафиксировать правильный ответ пользователя на вопрос
     * @param userId     ID пользователя
     * @param questionId ID вопроса
     */
    @Transactional
    public void recordCorrectAnswer(Long userId, Long questionId) {
        statsRepository.upsertCounters(userId, questionId, 1L, 0L, Instant.now());
    }

    /**
     * Зафиксировать неправильный ответ пользователя на вопрос
     * @param userId     ID пользователя
     * @param questionId ID вопроса
     */
    @Transactional
    public void recordWrongAnswer(Long userId, Long questionId) {
        statsRepository.upsertCounters(userId, questionId, 0L, 1L, Instant.now());
    }
}
