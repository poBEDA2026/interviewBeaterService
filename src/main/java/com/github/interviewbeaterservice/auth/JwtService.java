package com.github.interviewbeaterservice.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String issue(Long userId) {
        Date expiry = Date.from(Instant.now().plus(jwtProperties.ttl()));
        return Jwts.builder()
                .subject(userId.toString())
                .expiration(expiry)
                .signWith(secretKey())
                .compact();
    }

    public Long parse(String token) {
        String subject = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.parseLong(subject);
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
