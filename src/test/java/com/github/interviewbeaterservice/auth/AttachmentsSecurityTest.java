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

@WebMvcTest(controllers = QuestionAttachmentController.class)
@Import({ SecurityConfig.class, AuthExceptionHandler.class, AttachmentsSecurityTest.TestBeans.class })
@ActiveProfiles("test")
class AttachmentsSecurityTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtProperties jwtProperties;

    @MockitoBean AppUserDetailsService appUserDetailsService;
    @MockitoBean QuestionAttachmentService attachmentService;

    @TestConfiguration
    @EnableConfigurationProperties(JwtProperties.class)
    static class TestBeans {
        @Bean RestAuthenticationEntryPoint authEntryPoint() { return new RestAuthenticationEntryPoint(); }
        @Bean RestAccessDeniedHandler accessDeniedHandler() { return new RestAccessDeniedHandler(); }
        @Bean JwtService jwtService(JwtProperties props) { return new JwtService(props); }
    }

    @Test
    void postAttachmentsWithoutAuthHeaderReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void postAttachmentsWithNonBearerHeaderReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void postAttachmentsWithMalformedBearerReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

    @Test
    void postAttachmentsWithExpiredBearerReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Bearer " + issueExpiredAccessToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token expired"));
    }

    @Test
    void postAttachmentsWithRefreshTokenReturns401() throws Exception {
        mockMvc.perform(multipart("/questions/1/attachments")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "x".getBytes()))
                        .header("Authorization", "Bearer " + issueRefreshToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Bad credentials"));
    }

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

    @Test
    void deleteAttachmentsWithoutAuthHeaderReturns401() throws Exception {
        mockMvc.perform(delete("/questions/1/attachments/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void getAttachmentsWithoutAuthReturns200() throws Exception {
        when(attachmentService.list(1L)).thenReturn(List.of());

        mockMvc.perform(get("/questions/1/attachments"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
