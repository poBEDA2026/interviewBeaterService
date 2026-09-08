package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(BadCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException();
        }

        String token = jwtService.issue(user.getId());
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    }
}
