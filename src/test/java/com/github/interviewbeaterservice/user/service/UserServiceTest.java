package com.github.interviewbeaterservice.user.service;

import com.github.interviewbeaterservice.user.entity.Role;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    /** User.id uses @Setter(AccessLevel.NONE), so the field has no Lombok setter. */
    private static void setUserId(User u, Long id) {
        try {
            Field f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(u, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("register сохраняет юзера с bcrypt-хешем пароля и ролью USER")
    void register_savesUserWithEncodedPasswordAndUserRole() {
        when(passwordEncoder.encode("raw-pass")).thenReturn("$2a$10$hashedvalue");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.register("alice@example.com", "raw-pass");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getPassword()).isEqualTo("$2a$10$hashedvalue");
        assertThat(saved.getPassword()).startsWith("$2");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("register возвращает сохранённого юзера с присвоенным id")
    void register_returnsPersistedUserWithId() {
        when(passwordEncoder.encode("raw-pass")).thenReturn("$2a$10$hashedvalue");
        User persisted = User.builder().email("alice@example.com").password("$2a$10$hashedvalue").build();
        setUserId(persisted, 42L);
        when(userRepository.save(any(User.class))).thenReturn(persisted);

        User result = userService.register("alice@example.com", "raw-pass");

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
    }
}
