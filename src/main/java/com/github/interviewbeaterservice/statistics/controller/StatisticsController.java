package com.github.interviewbeaterservice.statistics.controller;

import com.github.interviewbeaterservice.auth.AuthContext;
import com.github.interviewbeaterservice.statistics.dto.QuestionStatsResponse;
import com.github.interviewbeaterservice.statistics.entity.UserQuestionStats;
import com.github.interviewbeaterservice.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("users/me/stats")
@Tag(name = "Statistics", description = "Методы для работы со статистикой пользователя")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Получение статистики пользователя
     * @return набор объектов {@link QuestionStatsResponse}
     */
    @Operation(summary = "Получение всей статистики пользователя")
    @GetMapping
    public List<QuestionStatsResponse> getAll() {
        Long userId = currentUserIdOrThrow();

        return statisticsService.getForUser(userId).stream()
                .map(StatisticsController::toResponse)
                .toList();
    }

    /**
     * Получение статистики пользователя по вопросу
     * @param questionId идентификатор вопроса
     * @return статистика в виде объекта {@link QuestionStatsResponse}
     */
    @Operation(summary = "Получение статистики пользователя по вопросу")
    @GetMapping("/{questionId}")
    public QuestionStatsResponse getByQuestion(@PathVariable Long questionId) {
        Long userId = currentUserIdOrThrow();

        return toResponse(statisticsService.getForUserAndQuestion(userId, questionId));
    }

    /**
     * Получение пользователя из контекста авторизации
     * @return id пользователя
     */
    private static Long currentUserIdOrThrow() {
        return AuthContext.currentUserId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    /**
     * Маппер типа {@link UserQuestionStats} к типу для ответа {@link QuestionStatsResponse}
     * @param stats статистика пользователя
     * @return статистика в виде объекта {@link QuestionStatsResponse}
     */
    private static QuestionStatsResponse toResponse(UserQuestionStats stats) {
        return new QuestionStatsResponse(
                stats.getQuestion().getId(),
                stats.getQuestion().getTitle(),
                stats.getCorrectCount(),
                stats.getWrongCount(),
                stats.getLastAnsweredAt()
        );
    }
}
