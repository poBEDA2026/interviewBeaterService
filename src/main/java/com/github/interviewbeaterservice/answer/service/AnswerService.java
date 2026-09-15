package com.github.interviewbeaterservice.answer.service;

import com.github.interviewbeaterservice.answer.dto.AnswerSubmissionResponse;
import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.answer.repository.AnswerRepository;
import com.github.interviewbeaterservice.question.service.QuestionService;
import com.github.interviewbeaterservice.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final StatisticsService statisticsService;

    /**
     * Принять ответ пользователя на вопрос, проверить корректность и обновить статистику.
     * Весь сценарий (валидация + инкремент счётчика) выполняется в одной транзакции.
     *
     * @param userId     ID авторизованного пользователя
     * @param questionId ID вопроса
     * @param answerId   ID выбранного варианта ответа
     * @return результат проверки
     * @throws EntityNotFoundException если вопрос или ответ не найдены,
     *                                 либо ответ не принадлежит указанному вопросу
     * @throws IllegalStateException   если у вопроса нет правильного ответа
     */
    @Transactional
    public AnswerSubmissionResponse submit(Long userId, Long questionId, Long answerId) {
        questionService.getById(questionId);

        Answer selected = answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Answer %s not found".formatted(answerId)));

        if (selected.getQuestion() == null || !selected.getQuestion().getId().equals(questionId)) {
            throw new EntityNotFoundException("Answer %s does not belong to this question".formatted(answerId));
        }

        Answer correct = answerRepository.findAllByQuestionId(questionId).stream()
                .filter(Answer::isCorrect)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Question %s has no correct answer configured".formatted(questionId)));

        if (selected.isCorrect()) {
            statisticsService.recordCorrectAnswer(userId, questionId);
        } else {
            statisticsService.recordWrongAnswer(userId, questionId);
        }

        return new AnswerSubmissionResponse(
                selected.isCorrect(),
                selected.getId(),
                correct.getId(),
                correct.getText()
        );
    }
}
