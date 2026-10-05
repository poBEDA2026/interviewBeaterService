package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import com.github.interviewbeaterservice.user.entity.Role;
import com.github.interviewbeaterservice.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.time.Duration;

import static com.github.interviewbeaterservice.utils.TestConstants.DEFAULT_EMAIL;
import static com.github.interviewbeaterservice.utils.TestConstants.PREFIX_ROLE;
import static com.github.interviewbeaterservice.utils.TestConstants.ROLE_USER;
import static com.github.interviewbeaterservice.utils.TestUtils.setField;
import static com.github.interviewbeaterservice.utils.TestUtils.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link AuthService}: логин через AuthenticationManager
 * и refresh по токену; {@link JwtService} реальный, менеджер и загрузчик — моки.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock AppUserDetailsService appUserDetailsService;

    JwtProperties jwtProperties = new JwtProperties(
            "test-secret-test-secret-test-secret-1234",
            Duration.ofMinutes(15),
            Duration.ofDays(7)
    );

    JwtService jwtService = new JwtService(jwtProperties);

    AuthService authService;

    private final User testUser = withId(
            User.builder().email(DEFAULT_EMAIL).password("hashed").build(),
            1L, Role.USER
    );

    /**
     * Собрать сервис перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtService, authenticationManager, appUserDetailsService);
        testUser.setEmail(DEFAULT_EMAIL);
        testUser.setPassword("hashed");
        setField(testUser, "role", Role.USER);
    }

    /**
     * Успешно аутентифицированный токен с принципалом тестового пользователя
     * @return токен, который возвращает AuthenticationManager при успешном логине
     */
    private Authentication authenticatedToken() {
        return new UsernamePasswordAuthenticationToken(
                new UserPrincipal(testUser), null, new UserPrincipal(testUser).getAuthorities());
    }

    /**
     * Логин с корректными кредами возвращает пару непустых различных токенов
     * и данные пользователя (id, email, роль с префиксом ROLE_)
     */
    @Test
    void loginReturnsAccessAndRefreshTokens() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticatedToken());

        LoginResponse resp = authService.login(DEFAULT_EMAIL, "raw");

        assertThat(resp.accessToken()).isNotBlank();
        assertThat(resp.refreshToken()).isNotBlank();
        assertThat(resp.accessToken()).isNotEqualTo(resp.refreshToken());
        assertThat(resp.userId()).isEqualTo(1L);
        assertThat(resp.email()).isEqualTo(DEFAULT_EMAIL);
        assertThat(resp.role()).isEqualTo(PREFIX_ROLE + ROLE_USER);
    }

    /**
     * Неверные креды от AuthenticationManager пробрасываются из сервиса как BadCredentialsException
     */
    @Test
    void loginPropagatesBadCredentialsFromAuthenticationManager() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(DEFAULT_EMAIL, "raw"))
                .isInstanceOf(BadCredentialsException.class);
    }

    /**
     * Refresh по валидному refresh-токену возвращает новую рабочую пару токенов
     */
    @Test
    void refreshWithValidRefreshTokenReturnsNewPair() throws AuthenticationException {
        String refresh = jwtService.issueRefresh(1L);
        when(appUserDetailsService.loadUserById(1L)).thenReturn(new UserPrincipal(testUser));

        LoginResponse resp = authService.refresh(refresh);

        assertThat(resp.accessToken()).isNotBlank();
        assertThat(resp.refreshToken()).isNotBlank();
        assertThat(resp.userId()).isEqualTo(1L);
        assertThat(resp.email()).isEqualTo(DEFAULT_EMAIL);
        assertThat(jwtService.parse(resp.accessToken(), TokenType.ACCESS)).isEqualTo(1L);
        assertThat(jwtService.parse(resp.refreshToken(), TokenType.REFRESH)).isEqualTo(1L);
    }

    /**
     * Refresh по access-токену отклоняется: тип токена не совпадает с ожидаемым
     */
    @Test
    void refreshWithAccessTokenThrowsBadCredentials() {
        String access = jwtService.issueAccess(1L);

        assertThatThrownBy(() -> authService.refresh(access))
                .isInstanceOf(BadCredentialsException.class);
    }

    /**
     * Refresh по просроченному refresh-токену бросает ExpiredJwtException
     */
    @Test
    void refreshWithExpiredRefreshTokenThrows() throws InterruptedException {
        JwtService shortLived = new JwtService(new JwtProperties(
                jwtProperties.secret(),
                jwtProperties.ttl(),
                Duration.ofMillis(1)
        ));
        String refresh = shortLived.issueRefresh(1L);
        Thread.sleep(50);

        assertThatThrownBy(() -> authService.refresh(refresh))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    /**
     * Refresh для неизвестного пользователя бросает BadCredentialsException
     */
    @Test
    void refreshForUnknownUserThrows() {
        String refresh = jwtService.issueRefresh(99L);
        when(appUserDetailsService.loadUserById(99L))
                .thenThrow(new BadCredentialsException("user not found"));

        assertThatThrownBy(() -> authService.refresh(refresh))
                .isInstanceOf(BadCredentialsException.class);
    }

    /**
     * Refresh по строке, не являющейся JWT, бросает исключение парсинга
     */
    @Test
    void refreshWithMalformedTokenThrows() {
        assertThatThrownBy(() -> authService.refresh("not-a-real-jwt"))
                .isInstanceOf(RuntimeException.class);
    }
}
