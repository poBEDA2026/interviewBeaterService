package com.github.interviewbeaterservice.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Обработчик отказа в доступе: отдаёт 403 с JSON-телом, когда
 * аутентифицированный пользователь без нужной роли обращается к эндпоинту.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    /**
     * Записать в ответ 403 и тело {"error":"Forbidden"}.
     * @param request запрос
     * @param response ответ
     * @param accessDeniedException исключение отказа в доступе
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Forbidden\"}");
    }
}
