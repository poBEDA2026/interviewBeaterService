package com.github.interviewbeaterservice.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  public static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;

  /**
   * Аутентификация по JWT из заголовка Authorization: Bearer.
   * userId кладётся в {@link AuthContext} и в SecurityContext — последний
   * нужен, чтобы правила .authenticated() в SecurityConfig пропускали запрос.
   * Без заголовка запрос проходит дальше анонимно; просроченный или
   * невалидный токен получает 401.
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
      // Кладём Authentication в SecurityContext, чтобы правила .authenticated() в SecurityConfig работали.
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, AuthorityUtils.NO_AUTHORITIES);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            writeError(response, "Token expired");
        } catch (SignatureException | MalformedJwtException e) {
            writeError(response, "Invalid token");
        } finally {
            AuthContext.clear();
            SecurityContextHolder.clearContext();
    }
  }

  private void writeError(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"" + message + "\"}");
  }
}
