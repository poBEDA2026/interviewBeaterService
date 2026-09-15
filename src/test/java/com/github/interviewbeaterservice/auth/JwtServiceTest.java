package com.github.interviewbeaterservice.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.security.sasl.AuthenticationException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-32chars";

    private JwtService newService(Duration ttl, Duration refreshTtl) {
        return new JwtService(new JwtProperties(SECRET, ttl, refreshTtl));
    }

    @Test
    @DisplayName("issue затем parse возвращают тот же userId")
    void issueAndParse_returnsSameUserId() throws AuthenticationException {
        JwtService service = newService(Duration.ofHours(1), Duration.ofDays(30));

        String token = service.issueAccess(42L);

        assertThat(service.parse(token, TokenType.ACCESS)).isEqualTo(42L);
    }

    @Test
    @DisplayName("parse истёкшего токена бросает ExpiredJwtException")
    void parse_expiredToken_throwsExpiredJwtException()
            throws InterruptedException, AuthenticationException {
        JwtService service = newService(Duration.ofMillis(1), Duration.ofDays(30));

        String token = service.issueAccess(7L);
        Thread.sleep(20);

        assertThatThrownBy(() -> service.parse(token, TokenType.ACCESS))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("parse токена с другой подписью бросает SignatureException")
    void parse_signedWithDifferentKey_throwsSignatureException()
            throws AuthenticationException {
        JwtService issuer = new JwtService(new JwtProperties(
                "other-secret-other-secret-other-secret",
                Duration.ofHours(1), Duration.ofDays(30)));
        JwtService verifier = newService(Duration.ofHours(1), Duration.ofDays(30));

        String token = issuer.issueAccess(99L);

        assertThatThrownBy(() -> verifier.parse(token, TokenType.ACCESS))
                .isInstanceOf(SignatureException.class);
    }
}
