package com.github.interviewbeaterservice.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.security.sasl.AuthenticationException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = JwtServiceTest.TestConfig.class)
@ActiveProfiles("test")
class JwtServiceTest {

  @EnableConfigurationProperties(JwtProperties.class)
  @Import(JwtService.class)
  static class TestConfig {

  }

  @Autowired
  JwtService jwtService;

  @Autowired
  JwtProperties jwtProperties;

  @Nested
  class AccessToken {

    @Test
    void roundtripsSubject() throws AuthenticationException {
      String token = jwtService.issueAccess(42L);
      assertThat(jwtService.parse(token, TokenType.ACCESS)).isEqualTo(42L);
    }

    @Test
    void refreshTokenCannotBeParsedAsAccess() {
      String refresh = jwtService.issueRefresh(42L);
      assertThatThrownBy(() -> jwtService.parse(refresh, TokenType.ACCESS))
          .isInstanceOf(AuthenticationException.class);
    }
  }

  @Nested
  class RefreshToken {

    @Test
    void roundtripsSubject() throws AuthenticationException {
      String token = jwtService.issueRefresh(42L);
      assertThat(jwtService.parse(token, TokenType.REFRESH)).isEqualTo(42L);
    }

    @Test
    void accessTokenCannotBeParsedAsRefresh() {
      String access = jwtService.issueAccess(42L);
      assertThatThrownBy(() -> jwtService.parse(access, TokenType.REFRESH))
          .isInstanceOf(AuthenticationException.class);
    }
  }

  @Nested
  class ExpiryAndSignature {

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
