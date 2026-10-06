package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.sasl.AuthenticationException;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  /**
   * Логин пользователя
   *
   * @param email    пользовательская почта
   * @param password пользовательский пароль
   * @return ответ, содержащий JWT и рефреш-токен {@link LoginResponse}
   */
  public LoginResponse login(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(BadCredentialsException::new);

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException();
    }

    return toResponse(user, jwtService.issueAccess(user.getId()),
        jwtService.issueRefresh(user.getId()));
  }

  /**
   * Обновление пользовательских токенов
   *
   * @param refreshToken рефреш-токен
   * @return ответ, содержащий JWT и рефреш-токен {@link LoginResponse}
   * @throws AuthenticationException
   */
  public LoginResponse refresh(String refreshToken) throws AuthenticationException {
    Long userId = jwtService.parse(refreshToken, TokenType.REFRESH);
    User user = userRepository.findById(userId)
        .orElseThrow(BadCredentialsException::new);

    return toResponse(user, jwtService.issueAccess(user.getId()),
        jwtService.issueRefresh(user.getId()));
  }

  /**
   * Маппер для создания объекта {@link LoginResponse}
   *
   * @param user         пользователь
   * @param accessToken  JWT
   * @param refreshToken рефреш-токен
   * @return тело ответа {@link LoginResponse}
   */
  private LoginResponse toResponse(User user, String accessToken, String refreshToken) {
    return new LoginResponse(accessToken, refreshToken, user.getId(), user.getEmail(),
        user.getRole().name());
  }
}
