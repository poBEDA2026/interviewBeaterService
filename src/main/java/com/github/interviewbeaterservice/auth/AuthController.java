package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginRequest;
import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import com.github.interviewbeaterservice.auth.dto.RefreshRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.security.sasl.AuthenticationException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Логин и выдача JWT")
public class AuthController {

  private final AuthService authService;

  /**
   * Логин по email/паролю
   *
   * @param requestBody тело запроса
   * @return ответ {@link LoginResponse} с JWT и рефреш-токеном
   */
  @Operation(summary = "Логин по email/паролю, возвращает JWT и рефреш-токен")
  @PostMapping("/login")
  public LoginResponse login(@RequestBody @Valid LoginRequest requestBody) {
    return authService.login(requestBody.email(), requestBody.password());
  }

  /**
   * Обновление JWT с помощью рефреш-токена
   *
   * @param requestBody тело запроса
   * @return обновлённые JWT и рефреш-токен
   * @throws AuthenticationException
   */
  @Operation(summary = "Обновление JWT с помощью рефреш-токена")
  @PostMapping("/refresh")
  public LoginResponse refresh(@RequestBody @Valid RefreshRequest requestBody)
      throws AuthenticationException {
    return authService.refresh(requestBody.refreshToken());
  }
}
