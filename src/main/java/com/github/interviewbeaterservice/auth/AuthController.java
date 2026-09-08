package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginRequest;
import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest requestBody) {
        return authService.login(requestBody.email(), requestBody.password());
    }
}
