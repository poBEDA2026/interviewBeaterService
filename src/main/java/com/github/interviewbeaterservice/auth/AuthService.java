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

    public LoginResponse login(String email, String password) {
        UserPrincipal user = (UserPrincipal) authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password))
                .getPrincipal();

        Long userId = user.getUserId();
        return toResponse(user, jwtService.issueAccess(userId), jwtService.issueRefresh(userId));
    }

    public LoginResponse refresh(String refreshToken) {
        Long userId = jwtService.parse(refreshToken, TokenType.REFRESH);
        UserPrincipal user = appUserDetailsService.loadUserById(userId);

        return toResponse(user, jwtService.issueAccess(userId), jwtService.issueRefresh(userId));
    }

    private LoginResponse toResponse(UserPrincipal user, String accessToken, String refreshToken) {
        String userRole = user.getAuthorities().iterator().next().getAuthority();
        return new LoginResponse(accessToken, refreshToken, user.getUserId(), user.getUsername(), userRole);
    }
}
