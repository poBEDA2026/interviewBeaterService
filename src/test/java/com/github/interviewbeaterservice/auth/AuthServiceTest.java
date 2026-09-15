package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import com.github.interviewbeaterservice.user.entity.Role;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.sasl.AuthenticationException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  UserRepository userRepository;
  @Mock
  PasswordEncoder passwordEncoder;

  JwtProperties jwtProperties = new JwtProperties(
      "test-secret-test-secret-test-secret-1234",
      Duration.ofMinutes(15),
      Duration.ofDays(7)
  );

  JwtService jwtService = new JwtService(jwtProperties);

  AuthService authService;

  private final User alice = withId(
      User.builder().email("alice@example.com").password("hashed").build(),
      1L, Role.USER
  );

  @BeforeEach
  void setUp() {
    authService = new AuthService(userRepository, passwordEncoder, jwtService);
    alice.setEmail("alice@example.com");
    alice.setPassword("hashed");
    setField(alice, "role", Role.USER);
  }

  private static User withId(User u, Long id, Role role) {
    setField(u, "id", id);
    setField(u, "role", role);
    return u;
  }

  private static void setField(Object target, String name, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void loginReturnsAccessAndRefreshTokens() {
    when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
    when(passwordEncoder.matches("raw", "hashed")).thenReturn(true);

    LoginResponse resp = authService.login("alice@example.com", "raw");

    assertThat(resp.accessToken()).isNotBlank();
    assertThat(resp.refreshToken()).isNotBlank();
    assertThat(resp.accessToken()).isNotEqualTo(resp.refreshToken());
    assertThat(resp.userId()).isEqualTo(1L);
    assertThat(resp.email()).isEqualTo("alice@example.com");
    assertThat(resp.role()).isEqualTo("USER");
  }

  @Test
  void loginWithWrongPasswordThrows() {
    when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(alice));
    when(passwordEncoder.matches("raw", "hashed")).thenReturn(false);

    assertThatThrownBy(() -> authService.login("alice@example.com", "raw"))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void loginWithUnknownEmailThrows() {
    when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login("ghost@example.com", "raw"))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void refreshWithValidRefreshTokenReturnsNewPair() throws AuthenticationException {
    String refresh = jwtService.issueRefresh(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

    LoginResponse resp = authService.refresh(refresh);

    assertThat(resp.accessToken()).isNotBlank();
    assertThat(resp.refreshToken()).isNotBlank();
    assertThat(resp.userId()).isEqualTo(1L);
    assertThat(resp.email()).isEqualTo("alice@example.com");
    // The new access token parses back to the same user and is typed ACCESS
    assertThat(jwtService.parse(resp.accessToken(), TokenType.ACCESS)).isEqualTo(1L);
    assertThat(jwtService.parse(resp.refreshToken(), TokenType.REFRESH)).isEqualTo(1L);
  }

  @Test
  void refreshWithAccessTokenThrows() {
    String access = jwtService.issueAccess(1L);

    assertThatThrownBy(() -> authService.refresh(access))
        .isInstanceOf(AuthenticationException.class);
  }

  @Test
  void refreshWithExpiredRefreshTokenThrows() throws InterruptedException {
    JwtService shortLived = new JwtService(new JwtProperties(
        jwtProperties.secret(),
        jwtProperties.ttl(),
        java.time.Duration.ofMillis(1)
    ));
    String refresh = shortLived.issueRefresh(1L);
    Thread.sleep(50);

    assertThatThrownBy(() -> authService.refresh(refresh))
        .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
  }

  @Test
  void refreshForUnknownUserThrows() {
    String refresh = jwtService.issueRefresh(99L);
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refresh(refresh))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void refreshWithMalformedTokenThrows() {
    assertThatThrownBy(() -> authService.refresh("not-a-real-jwt"))
        .isInstanceOf(RuntimeException.class);
  }
}
