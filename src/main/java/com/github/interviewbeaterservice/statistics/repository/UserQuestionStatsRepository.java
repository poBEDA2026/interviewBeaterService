package com.github.interviewbeaterservice.statistics.repository;

import com.github.interviewbeaterservice.statistics.entity.UserQuestionStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuestionStatsRepository extends JpaRepository<UserQuestionStats, Long> {

    /**
     * Получить статистику пользователя по конкретному вопросу
     * @param userId     ID пользователя
     * @param questionId ID вопроса
     * @return Optional с найденной записью или пустой
     */
    Optional<UserQuestionStats> findByUserIdAndQuestionId(Long userId, Long questionId);

    /**
     * Получить всю статистику пользователя
     * @param userId ID пользователя
     * @return список записей статистики
     */
    List<UserQuestionStats> findAllByUserId(Long userId);

    /**
     * Атомарный upsert счётчиков по паре (user_id, question_id).
     * Используется нативный INSERT ... ON CONFLICT, потому что JPA не даёт
     * истинного upsert по составному уникальному ключу, а read-then-write
     * через save() создал бы гонки между параллельными запросами.
     *
     * @param userId        ID пользователя
     * @param questionId    ID вопроса
     * @param correctDelta  сколько добавить к correct_count (0 или 1)
     * @param wrongDelta    сколько добавить к wrong_count (0 или 1)
     * @param now           момент последней попытки
     */
    @Modifying
    @Query(value = """
            INSERT INTO user_question_stats
                (user_id, question_id, correct_count, wrong_count, last_answered_at, created_at, updated_at)
            VALUES (:userId, :questionId, :correctDelta, :wrongDelta, :now, :now, :now)
            ON CONFLICT (user_id, question_id) DO UPDATE SET
                correct_count = user_question_stats.correct_count + EXCLUDED.correct_count,
                wrong_count = user_question_stats.wrong_count + EXCLUDED.wrong_count,
                last_answered_at = EXCLUDED.last_answered_at,
                updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    void upsertCounters(Long userId, Long questionId, long correctDelta, long wrongDelta, Instant now);
}
