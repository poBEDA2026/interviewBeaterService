package com.github.interviewbeaterservice.question.controller;

import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Регистрация пользователей")
public class QuestionController {

  private final QuestionService questionService;

  /**
   * Получить список всех вопросов.
   *
   * @return список всех вопросов
   */
  @Operation(summary = "Получение списка всех вопросов")
  @GetMapping
  public List<Question> getAll() {
    return questionService.getAll();
  }

  /**
   * Получить вопрос по его идентификатору.
   *
   * @param id идентификатор вопроса
   * @return найденный вопрос
   */
  @Operation(summary = "Получение вопроса по ID")
  @GetMapping("/{id}")
  public Question getById(@PathVariable Long id) {
    return questionService.getById(id);
  }

  /**
   * Создать новый вопрос.
   *
   * @param request тело запроса с заголовком вопроса
   * @return созданный вопрос
   */
  @Operation(summary = "Регистрация нового вопроса")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Question create(@RequestBody QuestionRequest request) {
    return questionService.create(request.title());
  }

  /**
   * Обновить заголовок существующего вопроса.
   *
   * @param id      идентификатор вопроса
   * @param request тело запроса с новым заголовком
   * @return обновлённый вопрос
   */
  @Operation(summary = "Редактирование заголовка вопроса")
  @PatchMapping("/{id}")
  public Question updateTitle(@PathVariable Long id, @RequestBody QuestionRequest request) {
    return questionService.updateTitle(id, request.title());
  }

  /**
   * Удалить вопрос по его идентификатору.
   *
   * @param id идентификатор вопроса
   * @return пустой ответ со статусом 204 No Content
   */
  @Operation(summary = "Удаление вопроса")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    questionService.delete(id);

    return ResponseEntity.noContent().build();
  }

  /**
   * Тело запроса для создания/редактирования вопроса.
   *
   * @param title заголовок вопроса
   */
  public record QuestionRequest(String title) {

  }
}
