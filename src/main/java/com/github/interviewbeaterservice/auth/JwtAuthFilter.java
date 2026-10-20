package com.github.interviewbeaterservice.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  public static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;

  /**
   * Извлечь JWT из заголовка {@code Authorization}, проверить его и положить userId в
   * {@link AuthContext} на время обработки запроса.
   *
   * @param request     HTTP-запрос
   * @param response    HTTP-ответ
   * @param filterChain цепочка фильтров
   * @throws ServletException если нижестоящий фильтр бросает исключение
   * @throws IOException      при ошибках ввода-вывода
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(BEARER_PREFIX.length());
    try {
      Long userId = jwtService.parse(token, TokenType.ACCESS);
      AuthContext.set(userId);
      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException e) {
      writeError(response, "Token expired");
    } catch (SignatureException | MalformedJwtException e) {
      writeError(response, "Invalid token");
    } finally {
      AuthContext.clear();
    }
  }

  /**
   * Записать в ответ статус 401 и JSON-тело с сообщением об ошибке.
   *
   * @param response HTTP-ответ
   * @param message  текст ошибки
   * @throws IOException при ошибках записи в ответ
   */
  private void writeError(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"" + message + "\"}");
  }
}
