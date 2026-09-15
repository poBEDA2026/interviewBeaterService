package com.github.interviewbeaterservice.user.controller;

import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JsonMapper jsonMapper;
    @MockitoBean UserService userService;

    private User userWithId(long id, String email) {
        User u = User.builder().email(email).password("hash").build();
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(u, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return u;
    }

    private String json(Map<String, ?> body) throws Exception {
        return jsonMapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("POST /auth/signup с валидным телом возвращает 200 и id в виде строки")
    void signup_validRequest_returns200AndIdString() throws Exception {
        when(userService.register("alice@example.com", "password123"))
                .thenReturn(userWithId(42L, "alice@example.com"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "alice@example.com",
                                "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @DisplayName("POST /auth/signup с невалидным email возвращает 400")
    void signup_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "not-an-email",
                                "password", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/signup с коротким паролем возвращает 400 (min=8)")
    void signup_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "alice@example.com",
                                "password", "short"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/signup при DataIntegrityViolationException пробрасывает исключение (текущее поведение — 500)")
    void signup_duplicateEmail_propagates() throws Exception {
        when(userService.register(anyString(), anyString()))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        // Known behavior: контроллер не обрабатывает DataIntegrityViolationException,
        // поэтому пробрасывается и превращается в 500. Когда появится глобальный обработчик,
        // тест нужно обновить на status().isConflict().
        // MockMvc 4.x по умолчанию пробрасывает исключения — оборачиваем.
        Throwable thrown = null;
        try {
            mockMvc.perform(post("/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of(
                            "email", "alice@example.com",
                            "password", "password123"))));
        } catch (Throwable t) {
            thrown = t;
        }
        assertThat(thrown).isNotNull();
        Throwable cause = thrown;
        while (cause != null && !(cause instanceof DataIntegrityViolationException)) {
            cause = cause.getCause();
        }
        assertThat(cause).isInstanceOf(DataIntegrityViolationException.class);
    }
}
