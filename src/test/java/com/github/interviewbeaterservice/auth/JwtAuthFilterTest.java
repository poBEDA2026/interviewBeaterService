package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.testsupport.TestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class JwtAuthFilterTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;

    @Test
    @DisplayName("без Authorization фильтр пропускает запрос, AuthContext = anonymous")
    void noAuthHeader_passesThrough() throws Exception {
        mockMvc.perform(get("/__test__/whoami"))
                .andExpect(status().isOk())
                .andExpect(content().string("anonymous"));
    }

    @Test
    @DisplayName("валидный Bearer-токен прокидывает userId в AuthContext")
    void validBearerToken_setsAuthContext() throws Exception {
        String token = jwtService.issue(42L);

        mockMvc.perform(get("/__test__/whoami").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @DisplayName("AuthContext очищается после запроса с токеном")
    void authContext_isClearedAfterRequest() throws Exception {
        String token = jwtService.issue(42L);

        // с токеном
        mockMvc.perform(get("/__test__/whoami").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));

        // следующий запрос без токена должен видеть anonymous
        mockMvc.perform(get("/__test__/whoami"))
                .andExpect(status().isOk())
                .andExpect(content().string("anonymous"));
    }

    @Test
    @DisplayName("истёкший токен возвращает 401 с сообщением Token expired")
    void expiredToken_returns401() throws Exception {
        JwtService shortLived = new JwtService(new JwtProperties(
                "test-secret-test-secret-test-secret-32chars",
                java.time.Duration.ofMillis(1)));
        String token = shortLived.issue(42L);
        Thread.sleep(20);

        mockMvc.perform(get("/__test__/whoami").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Token expired")));
    }

    @Test
    @DisplayName("токен с чужой подписью возвращает 401 с сообщением Invalid token")
    void invalidSignature_returns401() throws Exception {
        JwtService otherKey = new JwtService(new JwtProperties(
                "other-secret-other-secret-other-secret",
                java.time.Duration.ofHours(1)));
        String token = otherKey.issue(42L);

        mockMvc.perform(get("/__test__/whoami").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid token")));
    }

    @Test
    @DisplayName("невалидный JWT возвращает 401")
    void malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/__test__/whoami").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid token")));
    }

    @Test
    @DisplayName("не-Bearer схема Authorization пропускается фильтром")
    void nonBearerScheme_passesThrough() throws Exception {
        mockMvc.perform(get("/__test__/whoami").header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isOk())
                .andExpect(content().string("anonymous"));
    }
}
