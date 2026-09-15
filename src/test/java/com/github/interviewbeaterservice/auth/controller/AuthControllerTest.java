package com.github.interviewbeaterservice.auth.controller;

import com.github.interviewbeaterservice.auth.AuthController;
import com.github.interviewbeaterservice.auth.AuthService;
import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    /** Boot 4 + Jackson 3: используем JsonMapper (новый Jackson API). */
    @Autowired JsonMapper jsonMapper;
    @MockitoBean AuthService authService;

    private String json(Map<String, ?> body) throws Exception {
        return jsonMapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("POST /auth/login с валидным телом возвращает 200 и поля token/userId/email/role")
    void login_validRequest_returns200() throws Exception {
        when(authService.login("alice@example.com", "raw"))
                .thenReturn(new LoginResponse("jwt.token", 7L, "alice@example.com", "USER"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "alice@example.com",
                                "password", "raw"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token"))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /auth/login с невалидным email возвращает 400")
    void login_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "not-an-email",
                                "password", "raw"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login с пустым паролем возвращает 400")
    void login_blankPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "alice@example.com",
                                "password", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login без тела возвращает 400")
    void login_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login при BadCredentialsException пробрасывает исключение (текущее поведение — 500)")
    void login_badCredentials_propagates() throws Exception {
        when(authService.login(anyString(), anyString()))
                .thenThrow(new BadCredentialsException());

        // Known behavior: контроллер не имеет @RestControllerAdvice для BadCredentialsException,
        // поэтому исключение пробрасывается и превращается в 500.
        // Когда появится глобальный обработчик, тест нужно обновить на status().isUnauthorized().
        // MockMvc 4.x по умолчанию пробрасывает исключения в тест — оборачиваем в try/catch.
        Throwable thrown = null;
        try {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of(
                            "email", "alice@example.com",
                            "password", "wrong"))));
        } catch (Throwable t) {
            thrown = t;
        }
        assertThat(thrown).isNotNull();
        // ServletException wraps the original; underlying cause should be BadCredentialsException
        Throwable cause = thrown;
        while (cause != null && !(cause instanceof BadCredentialsException)) {
            cause = cause.getCause();
        }
        assertThat(cause).isInstanceOf(BadCredentialsException.class);
    }
}
