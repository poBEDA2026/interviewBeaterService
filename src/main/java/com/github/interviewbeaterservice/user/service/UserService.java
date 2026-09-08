package com.github.interviewbeaterservice.user.service;

import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String email, String rawPassword) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .build();

        return userRepository.save(user);
    }
}