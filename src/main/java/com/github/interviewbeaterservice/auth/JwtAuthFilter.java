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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AppUserDetailsService appUserDetailsService;

    /**
     * Аутентификация по JWT из заголовка Authorization: Bearer.
     * userId кладётся в {@link AuthContext}, а {@link UserPrincipal} с ролями —
     * в SecurityContext, чтобы правила .authenticated()/.hasRole() в SecurityConfig работали.
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
            UserPrincipal userPrincipal = appUserDetailsService.loadUserById(userId);
            AuthContext.set(userId);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities()));

            filterChain.doFilter(request, response);
        } catch (UsernameNotFoundException e) {
            writeError(response, "User not found");
        } catch (ExpiredJwtException e) {
            writeError(response, "Token expired");
        } catch (SignatureException | MalformedJwtException e) {
            writeError(response, "Invalid token");
        } catch (AuthenticationException e) {
            writeError(response, "Bad credentials");
        } finally {
            AuthContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Записать в ответ 401 с JSON-телом {"error": message}.
     * @param response ответ
     * @param message текст ошибки
     */
    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
