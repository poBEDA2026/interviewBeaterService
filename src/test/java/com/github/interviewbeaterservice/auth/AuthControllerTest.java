package com.github.interviewbeaterservice.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import com.github.interviewbeaterservice.auth.exception.AuthExceptionHandler;
import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.security.sasl.AuthenticationException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)

@Import(AuthExceptionHandler.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean AuthService authService;

    @Test
    void postRefreshReturnsNewTokens() throws Exception {
        LoginResponse resp = new LoginResponse("access-1", "refresh-1", 7L, "u@x.com", "USER");
        when(authService.refresh(anyString())).thenReturn(resp);

        String body = objectMapper.writeValueAsString(Map.of("refreshToken", "rt-abc"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-1"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-1"))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.email").value("u@x.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void postRefreshWithBlankTokenReturns400() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postRefreshWithBadCredentialsReturns401() throws Exception {
        when(authService.refresh(anyString())).thenThrow(new BadCredentialsException());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"some-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Bad credentials"));
    }

    @Test
    void postRefreshWithInvalidTypeReturns401() throws Exception {
        when(authService.refresh(anyString())).thenThrow(new AuthenticationException("Token type mismatch"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"some-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Bad credentials"));
    }

    @Test
    void postLoginReturnsNewShapeWithBothTokens() throws Exception {
        LoginResponse resp = new LoginResponse("a", "r", 1L, "alice@x.com", "USER");
        when(authService.login(anyString(), anyString())).thenReturn(resp);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@x.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("a"))
                .andExpect(jsonPath("$.refreshToken").value("r"));
    }
}
