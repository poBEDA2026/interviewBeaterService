package com.github.interviewbeaterservice.user.controller;

import com.github.interviewbeaterservice.user.dto.RegisterRequest;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Регистрация пользователей")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Регистрация нового пользователя, возвращает id")
    @PostMapping("/signup")
    public String signup(@RequestBody @Valid RegisterRequest requestBody) {
        User createdUser = userService.register(requestBody.email(), requestBody.password());

        return createdUser.getId().toString();
    }
}
