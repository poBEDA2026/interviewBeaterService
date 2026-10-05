package com.github.interviewbeaterservice.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Точка входа для неаутентифицированных запросов: отдает 401 с JSON-телом
 * вместо редиректа на страницу логина.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Записать в ответ 401 и тело {"error":"Unauthorized"}.
     * @param request запрос
     * @param response ответ
     * @param authException исключение, приведшее к отказу
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Unauthorized\"}");
    }
}
