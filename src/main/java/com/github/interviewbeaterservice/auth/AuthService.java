package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService appUserDetailsService;

    /**
     * Войти по email и паролю: аутентификация через {@link AuthenticationManager},
     * при успехе выпускается новая пара access/refresh-токенов.
     * @param email email пользователя
     * @param password пароль в открытом виде
     * @return ответ с парой токенов и данными пользователя
     */
    public LoginResponse login(String email, String password) {
        UserPrincipal user = (UserPrincipal) authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password))
                .getPrincipal();

        Long userId = user.getUserId();
        return toResponse(user, jwtService.issueAccess(userId), jwtService.issueRefresh(userId));
    }

    /**
     * Выдать новую пару токенов по валидному refresh-токену.
     * @param refreshToken refresh-токен из запроса
     * @return ответ с новой парой access/refresh-токенов и данными пользователя
     */
    public LoginResponse refresh(String refreshToken) {
        Long userId = jwtService.parse(refreshToken, TokenType.REFRESH);
        UserPrincipal user = appUserDetailsService.loadUserById(userId);

        return toResponse(user, jwtService.issueAccess(userId), jwtService.issueRefresh(userId));
    }

    /**
     * Собрать ответ логина из принципала и пары токенов;
     * роль берётся из authority пользователя (ROLE_XXX).
     * @param user принципал пользователя
     * @param accessToken access-токен
     * @param refreshToken refresh-токен
     * @return готовый ответ
     */
    private LoginResponse toResponse(UserPrincipal user, String accessToken, String refreshToken) {
        String userRole = user.getAuthorities().iterator().next().getAuthority();
        return new LoginResponse(accessToken, refreshToken, user.getUserId(), user.getUsername(), userRole);
    }
}
