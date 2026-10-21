package com.github.interviewbeaterservice.answer.controller;

import com.github.interviewbeaterservice.answer.dto.AnswerOption;
import com.github.interviewbeaterservice.answer.dto.AnswerSubmissionResponse;
import com.github.interviewbeaterservice.answer.service.AnswerService;
import com.github.interviewbeaterservice.auth.AuthContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/questions/{questionId}/answers")
@Tag(name = "Answers", description = "Методы для работы с ответами")
public class AnswerController
{
    private final AnswerService answerService;

    /**
     * Метод для получения списка вариантов ответа на вопрос.
     * Признак правильности не отдается, чтобы ответ нельзя было подсмотреть в devtools.
     * @param questionId идентификатор вопроса
     * @return список вариантов ответа
     */
    @Operation(summary = "Получение вариантов ответа на вопрос")
    @GetMapping
    public List<AnswerOption> listOptions(@PathVariable Long questionId)
    {
        return answerService.listOptions(questionId);
    }

    /**
     * Метод для предоставления ответа на вопрос
     * @param questionId идентификатор ответа
     * @param request тело запроса
     * @return ответ типа {@link AnswerSubmissionResponse}
     */
    @Operation(summary = "Предоставление ответа на вопрос")
    @PostMapping
    public AnswerSubmissionResponse submit(@PathVariable Long questionId,
                                           @RequestBody AnswerSubmissionRequest request)
    {
        Long userId = AuthContext.currentUserId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));

        return answerService.submit(userId, questionId, request.answerId());
    }

    public record AnswerSubmissionRequest(Long answerId)
    {

    }
}
