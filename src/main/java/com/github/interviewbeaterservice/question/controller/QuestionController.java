package com.github.interviewbeaterservice.question.controller;

import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Регистрация пользователей")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "Получение списка всех вопросов")
    @GetMapping
    public List<Question> getAll() {
        return questionService.getAll();
    }

    @Operation(summary = "Получение вопроса по ID")
    @GetMapping("/{id}")
    public Question getById(@PathVariable Long id) {
        return questionService.getById(id);
    }

    @Operation(summary = "Регистрация нового вопроса")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Question create(@RequestBody QuestionRequest request) {
        return questionService.create(request.title());
    }

    @Operation(summary = "Редактирование заголовка вопроса")
    @PatchMapping("/{id}")
    public Question updateTitle(@PathVariable Long id, @RequestBody QuestionRequest request) {
        return questionService.updateTitle(id, request.title());
    }

    @Operation(summary = "Удаление вопроса")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);

        return ResponseEntity.noContent().build();
    }

    public record QuestionRequest(String title) {
    }
}
