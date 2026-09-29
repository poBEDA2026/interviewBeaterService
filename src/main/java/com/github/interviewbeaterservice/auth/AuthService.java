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

    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(BadCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException();
        }

        return toResponse(user, jwtService.issueAccess(user.getId()), jwtService.issueRefresh(user.getId()));
    }

    public LoginResponse refresh(String refreshToken) throws AuthenticationException {
        Long userId = jwtService.parse(refreshToken, TokenType.REFRESH);
        User user = userRepository.findById(userId)
                .orElseThrow(BadCredentialsException::new);

        return toResponse(user, jwtService.issueAccess(user.getId()), jwtService.issueRefresh(user.getId()));
    }

    private LoginResponse toResponse(User user, String accessToken, String refreshToken) {
        return new LoginResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getRole().name());
    }
}
