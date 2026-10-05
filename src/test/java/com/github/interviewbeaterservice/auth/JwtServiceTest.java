package com.github.interviewbeaterservice.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тесты {@link JwtService} на минимальном контексте: roundtrip subject,
 * несовпадение типа токена, истечение срока и подпись чужим секретом.
 */
@SpringBootTest(classes = JwtServiceTest.TestConfig.class)
@ActiveProfiles("test")
class JwtServiceTest {

    /**
     * Минимальный контекст: только JwtProperties и JwtService, без остального приложения
     */
    @EnableConfigurationProperties(JwtProperties.class)
    @Import(JwtService.class)
    static class TestConfig {}

    @Autowired
    JwtService jwtService;

    @Autowired
    JwtProperties jwtProperties;

    /**
     * Тесты access-токенов
     */
    @Nested
    class AccessToken {

        /**
         * Выпущенный access-токен парсится обратно и возвращает id из subject
         */
        @Test
        void roundtripsSubject() throws AuthenticationException {
            String token = jwtService.issueAccess(42L);
            assertThat(jwtService.parse(token, TokenType.ACCESS)).isEqualTo(42L);
        }

        /**
         * Refresh-токен не принимается как access: бросается BadCredentialsException
         */
        @Test
        void refreshTokenCannotBeParsedAsAccess() {
            String refresh = jwtService.issueRefresh(42L);
            assertThatThrownBy(() -> jwtService.parse(refresh, TokenType.ACCESS))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    /**
     * Тесты refresh-токенов
     */
    @Nested
    class RefreshToken {

        /**
         * Выпущенный refresh-токен парсится обратно и возвращает id из subject
         */
        @Test
        void roundtripsSubject() throws AuthenticationException {
            String token = jwtService.issueRefresh(42L);
            assertThat(jwtService.parse(token, TokenType.REFRESH)).isEqualTo(42L);
        }

        /**
         * Access-токен не принимается как refresh: бросается BadCredentialsException
         */
        @Test
        void accessTokenCannotBeParsedAsRefresh() {
            String access = jwtService.issueAccess(42L);
            assertThatThrownBy(() -> jwtService.parse(access, TokenType.REFRESH))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    /**
     * Тесты срока действия и подписи
     */
    @Nested
    class ExpiryAndSignature {

        /**
         * Просроченный access-токен бросает ExpiredJwtException при парсинге
         */
        @Test
        void expiredAccessTokenThrowsExpiredJwtException() throws InterruptedException {
            JwtProperties shortTtl = new JwtProperties(
                    jwtProperties.secret(), Duration.ofMillis(1), jwtProperties.refreshTtl());
            JwtService shortLived = new JwtService(shortTtl);
            String token = shortLived.issueAccess(42L);

            Thread.sleep(50);

            assertThatThrownBy(() -> jwtService.parse(token, TokenType.ACCESS))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        /**
         * Токен, подписанный другим секретом, бросает SignatureException
         */
        @Test
        void badSignatureThrows() {
            JwtService other = new JwtService(new JwtProperties(
                    "different-secret-different-secret-1234",
                    jwtProperties.ttl(),
                    jwtProperties.refreshTtl()
            ));
            String foreignToken = other.issueAccess(42L);

            assertThatThrownBy(() -> jwtService.parse(foreignToken, TokenType.ACCESS))
                    .isInstanceOf(SignatureException.class);
        }
    }
}
