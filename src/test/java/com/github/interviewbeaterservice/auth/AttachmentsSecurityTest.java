package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.exception.AuthExceptionHandler;
import com.github.interviewbeaterservice.config.SecurityConfig;
import com.github.interviewbeaterservice.question.controller.QuestionAttachmentController;
import com.github.interviewbeaterservice.question.dto.AttachmentResponse;
import com.github.interviewbeaterservice.question.service.QuestionAttachmentService;
import com.github.interviewbeaterservice.utils.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.github.interviewbeaterservice.utils.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты правил доступа к эндпоинтам вложений:
 * без токена или с некорректным Bearer-токеном любые запросы (GET/POST/DELETE)
 * получают 401, с валидным access-токеном запрос доходит до контроллера.
 */
@WebMvcTest(controllers = QuestionAttachmentController.class)
@Import({ SecurityConfig.class, AuthExceptionHandler.class, AttachmentsSecurityTest.TestBeans.class })
@ActiveProfiles("test")
class AttachmentsSecurityTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtProperties jwtProperties;

    @MockitoBean AppUserDetailsService appUserDetailsService;
    @MockitoBean QuestionAttachmentService attachmentService;

    /**
     * Реальные entry point/handler и JwtService для security-цепочки,
     * чтобы фильтр из SecurityConfig проверял токены по-настоящему.
     */
    @TestConfiguration
    @EnableConfigurationProperties(JwtProperties.class)
    static class TestBeans {
        @Bean RestAuthenticationEntryPoint authEntryPoint() { return new RestAuthenticationEntryPoint(); }
        @Bean RestAccessDeniedHandler accessDeniedHandler() { return new RestAccessDeniedHandler(); }
        @Bean JwtService jwtService(JwtProperties props) { return new JwtService(props); }
    }

    /**
     * Загрузка вложения без заголовка Authorization получает 401 Unauthorized
     */
    @Test
    void postAttachmentsWithoutAuthHeaderReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /**
     * Загрузка вложения с не-Bearer схемой (Basic) получает 401 Unauthorized
     */
    @Test
    void postAttachmentsWithNonBearerHeaderReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /**
     * Загрузка вложения с мусорной строкой вместо JWT получает 401 Invalid token
     */
    @Test
    void postAttachmentsWithMalformedBearerReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

    /**
     * Загрузка вложения с просроченным access-токеном получает 401 Token expired
     */
    @Test
    void postAttachmentsWithExpiredBearerReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Bearer " + issueExpiredAccessToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token expired"));
    }

    /**
     * Загрузка вложения с refresh-токеном вместо access-токена получает 401 Bad credentials
     */
    @Test
    void postAttachmentsWithRefreshTokenReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Bearer " + issueRefreshToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Bad credentials"));
    }

    /**
     * Загрузка вложения с валидным access-токеном проходит фильтр и доходит до контроллера (200)
     */
    @Test
    void postAttachmentsWithValidBearerReachesController() throws Exception {
        when(appUserDetailsService.loadUserById(TestConstants.DEFAULT_USER_ID)).thenReturn(principalFor(TestConstants.DEFAULT_USER_ID));
        when(attachmentService.upload(anyLong(), any())).thenReturn(List.<AttachmentResponse>of());

        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Bearer " + issueValidAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    /**
     * Удаление вложения без заголовка Authorization получает 401 Unauthorized
     */
    @Test
    void deleteAttachmentsWithoutAuthHeaderReturns401() throws Exception {
        mockMvc.perform(delete("/questions/1/attachments/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    /**
     * GET списка вложений без авторизации получает 401 Unauthorized
     */
    @Test
    void getAttachmentsWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/questions/1/attachments"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
