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

    public String issueAccess(Long userId)
    {
        return issue(userId, jwtProperties.ttl(), TokenType.ACCESS);
    }

    public String issueRefresh(Long userId)
    {
        return issue(userId, jwtProperties.refreshTtl(), TokenType.REFRESH);
    }

    public Long parse(String token, TokenType expected) throws AuthenticationException {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String type = claims.get("typ", String.class);
        String expectedType = expected.claim();

        if (!expectedType.equals(type)) {
            throw new AuthenticationException("Token type mismatch: expected " + expectedType + ", got " + type);
        }

        return Long.parseLong(claims.getSubject());
    }

    private String issue(Long userId, Duration ttl, TokenType type)
    {
        Date expiry = Date.from(Instant.now().plus(ttl));

        return Jwts.builder()
                .subject(userId.toString())
                .claim("typ", type.claim())
                .expiration(expiry)
                .signWith(secretKey())
                .compact();
    }

    private SecretKey secretKey()
    {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
