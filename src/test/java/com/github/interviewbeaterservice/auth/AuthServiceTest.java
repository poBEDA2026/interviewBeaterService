package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.auth.dto.LoginResponse;
import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import com.github.interviewbeaterservice.user.entity.Role;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks AuthService authService;

    private User userWithIdAndRole(long id, String email, String hash, Role role) {
        User u = User.builder().email(email).password(hash).build();
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(u, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        u.setRole(role);
        return u;
    }

    @Test
    @DisplayName("login при валидных кредов возвращает LoginResponse со всеми полями")
    void login_validCredentials_returnsLoginResponse() {
        User user = userWithIdAndRole(7L, "alice@example.com", "$2a$10$hash", Role.ADMIN);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "$2a$10$hash")).thenReturn(true);
        when(jwtService.issue(7L)).thenReturn("jwt.token.value");

        LoginResponse response = authService.login("alice@example.com", "raw");

        assertThat(response.token()).isEqualTo("jwt.token.value");
        assertThat(response.userId()).isEqualTo(7L);
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("login для несуществующего email бросает BadCredentialsException")
    void login_unknownEmail_throwsBadCredentials() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("ghost@example.com", "raw"))
                .isInstanceOf(BadCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).issue(any());
    }

    @Test
    @DisplayName("login с неверным паролем бросает BadCredentialsException")
    void login_wrongPassword_throwsBadCredentials() {
        User user = userWithIdAndRole(7L, "alice@example.com", "$2a$10$hash", Role.USER);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("alice@example.com", "wrong"))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).issue(any());
    }
}
