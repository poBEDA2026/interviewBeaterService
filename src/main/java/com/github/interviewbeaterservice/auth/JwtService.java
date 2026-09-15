package com.github.interviewbeaterservice.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.security.sasl.AuthenticationException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtProperties jwtProperties;

  /**
   * Вылача JWT-токена пользователю
   *
   * @param userId идентификатор пользователя
   * @return JWT-токен
   */
  public String issueAccess(Long userId) {
    return issue(userId, jwtProperties.ttl(), TokenType.ACCESS);
  }

  /**
   * Вылача рефрещ-токена пользователю
   *
   * @param userId идентификатор пользователя
   * @return рефрещ-токен
   */
  public String issueRefresh(Long userId) {
    return issue(userId, jwtProperties.refreshTtl(), TokenType.REFRESH);
  }

  /**
   * Считывание идентификатор пользователя из токена
   *
   * @param token    токен
   * @param expected ожидаемый тип токена
   * @return идентификатор пользователя
   * @throws AuthenticationException
   */
  public Long parse(String token, TokenType expected) throws AuthenticationException {
    Claims claims = Jwts.parser()
        .verifyWith(secretKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    String type = claims.get("typ", String.class);
    String expectedType = expected.claim();

    if (!expectedType.equals(type)) {
      throw new AuthenticationException(
          "Token type mismatch: expected " + expectedType + ", got " + type);
    }

    return Long.parseLong(claims.getSubject());
  }

  /**
   * Выдача токена пользователю
   *
   * @param userId идентификатор пользователя
   * @param ttl    время жизни токена
   * @param type   тип токена
   * @return токен
   */
  private String issue(Long userId, Duration ttl, TokenType type) {
    Date expiry = Date.from(Instant.now().plus(ttl));

    return Jwts.builder()
        .subject(userId.toString())
        .claim("typ", type.claim())
        .expiration(expiry)
        .signWith(secretKey())
        .compact();
  }

    /**
     * Получение ключа для подписи JWT
     * @return ключ {@link SecretKey}
     */
  private SecretKey secretKey() {
    return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
  }
}
