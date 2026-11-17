package com.github.interviewbeaterservice.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.interviewbeaterservice.answer.repository.AnswerRepository;
import com.github.interviewbeaterservice.auth.JwtAuthFilter;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.exception.ReviewSetNotFoundException;
import com.github.interviewbeaterservice.review.repository.ReviewSetItemRepository;
import com.github.interviewbeaterservice.review.service.ReviewSetService;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Тесты HTTP-слоя контроллера {@link ReviewSetController}: сериализация ответов,
 * валидация входного JSON и маппинг доменных исключений в коды ответа.
 */
@WebMvcTest(controllers = ReviewSetController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class ReviewSetControllerTest {

  @Autowired
  MockMvc mockMvc;
  @MockitoBean
  ReviewSetService service;
  @MockitoBean
  ReviewSetItemRepository itemRepository;
  @MockitoBean
  QuestionRepository questionRepository;
  @MockitoBean
  AnswerRepository answerRepository;

  @Test
  @DisplayName("Создание подборки возвращает 200 и идентификатор сохранённой подборки")
  void createReturns200() throws Exception {
    when(service.create(any())).thenReturn(setWithId(7L));
    String body = """
        {"title":"Core Java","description":"desc","questionIds":[1,2,3]}
        """;
    mockMvc.perform(post("/review-sets")
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.title").value("Core Java"));
  }

  @Test
  @DisplayName("Создание подборки с некорректным телом запроса возвращает 400")
  void createValidationError400() throws Exception {
    String body = """
        {"title":"","questionIds":[]}
        """;
    mockMvc.perform(post("/review-sets")
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Чтение несуществующей подборки возвращает 404 с сообщением об ошибке")
  void getByIdReturns404WhenMissing() throws Exception {
    when(service.getById(7L)).thenThrow(new ReviewSetNotFoundException(7L));
    mockMvc.perform(get("/review-sets/7"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Review set 7 not found"));
  }

  @Test
  @DisplayName("Список подборок возвращает массив из всех сохранённых подборок")
  void listReturnsArray() throws Exception {
    when(service.listAll()).thenReturn(List.of(setWithId(1L), setWithId(2L)));
    mockMvc.perform(get("/review-sets"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  private static ReviewSet setWithId(Long id) {
    ReviewSet s = ReviewSet.builder().title("Core Java").description("d").build();
    try {
      Field f = ReviewSet.class.getDeclaredField("id");
      f.setAccessible(true);
      f.set(s, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return s;
  }
}
