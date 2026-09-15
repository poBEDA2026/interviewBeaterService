package com.github.interviewbeaterservice.config;

import com.github.interviewbeaterservice.testsupport.TestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;

    /**
     * SecurityConfig объявляет "/api/swagger-ui.html" как permitAll. В тестовом MockMvc-контексте
     * springdoc-openapi не инициализирует свой сервлет (отсутствует автоконфигурация для swagger-ui
     * в springdoc-openapi-starter-webmvc-ui при отсутствии запросов к api-docs), поэтому запрос
     * пройдёт security-цепочку (без 401/403) и упрётся в 404 от диспетчера. Главное, что мы
     * проверяем здесь — что SecurityConfig не блокирует путь.
     */
    @Test
    @DisplayName("swagger-ui путь проходит security-цепочку без 401/403")
    void swaggerPath_isNotBlockedBySecurity() throws Exception {
        mockMvc.perform(get("/api/swagger-ui.html"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status)
                            .as("SecurityConfig must NOT block /api/swagger-ui.html — 401/403 forbidden")
                            .isNotIn(401, 403);
                });
    }

    @Test
    @DisplayName("произвольный путь доступен без авторизации (anyRequest permitAll — текущее поведение)")
    void anyRequest_returns200_perCurrentBehavior() throws Exception {
        mockMvc.perform(get("/__test__/whoami"))
                .andExpect(status().isOk());
    }
}
