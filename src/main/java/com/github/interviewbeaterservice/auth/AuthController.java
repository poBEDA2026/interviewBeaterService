package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginRequest;
import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Логин и выдача JWT")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Логин по email/паролю, возвращает JWT")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest requestBody) {
        return authService.login(requestBody.email(), requestBody.password());
    }
}
