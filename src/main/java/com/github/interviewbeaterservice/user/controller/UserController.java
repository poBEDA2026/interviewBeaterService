package com.github.interviewbeaterservice.user.controller;

import com.github.interviewbeaterservice.user.dto.RegisterRequest;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public String signup(@RequestBody @Valid RegisterRequest requestBody) {
        User createdUser = userService.register(requestBody.email(), requestBody.password());

        return createdUser.getId().toString();
    }
}
