package com.github.interviewbeaterservice.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Выпуск и проверка JWT-токенов (access и refresh), подписанных
 * HMAC-секретом из {@link JwtProperties}.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtProperties jwtProperties;

  /**
   * Выдача access-токена пользователю
   *
   * @param userId идентификатор пользователя
   * @return JWT-токен
   */
  public String issueAccess(Long userId) {
    return issue(userId, jwtProperties.ttl(), TokenType.ACCESS);
  }

  /**
   * Выдача refresh-токена пользователю
   *
   * @param userId идентификатор пользователя
   * @return refresh-токен
   */
  public String issueRefresh(Long userId) {
    return issue(userId, jwtProperties.refreshTtl(), TokenType.REFRESH);
  }

  /**
   * Считывание идентификатора пользователя из токена
   *
   * @param token    токен
   * @param expected ожидаемый тип токена
   * @return идентификатор пользователя
   * @throws AuthenticationException, если токен просрочен, подписан другим ключом или его тип не совпадает с ожидаемым
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
      throw new BadCredentialsException(
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
