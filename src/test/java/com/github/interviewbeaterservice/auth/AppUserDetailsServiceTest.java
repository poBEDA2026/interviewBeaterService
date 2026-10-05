package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.user.entity.Role;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static com.github.interviewbeaterservice.utils.TestConstants.*;
import static com.github.interviewbeaterservice.utils.TestUtils.withId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppUserDetailsServiceTest {

    @Mock UserRepository userRepository;

    @Mock AppUserDetailsService appUserDetailsService;

    /**
     * Тестовый пользователь
     */
    private final User testUser = withId(
            User.builder().email(DEFAULT_EMAIL).password("hashed").build(),
            1L, Role.USER
    );


    @BeforeEach
    void setUp() {
        appUserDetailsService = new AppUserDetailsService(userRepository);
    }

    /**
     * Проверка, что для пользователя в контексте возвращаются корректные данные
     */
    @Test
    void loadByEmailReturnsPrincipalWithDetails() {
        when(userRepository.findByEmail(DEFAULT_EMAIL)).thenReturn(Optional.of(testUser));
        UserPrincipal userPrincipal = appUserDetailsService.loadUserByUsername(DEFAULT_EMAIL);

        assertThat(userPrincipal.getUserId()).isEqualTo(1L);
        assertThat(userPrincipal.getUsername()).isEqualTo(DEFAULT_EMAIL);

        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        assertThat(authorities).size().isEqualTo(1);
        assertThat(authorities.iterator().next().getAuthority())
                .isEqualTo(PREFIX_ROLE + ROLE_USER);
    }

    /**
     * Проверка, что для админа возвращается соответствующая роль
     */
    @Test
    void loadByAdminReturnsRoleAdminAuthority() {
        String adminEmail = "admin@test.ru";
        User testAdmin = withId(
                User.builder().email(adminEmail).password("hashed").build(),
                2L, Role.ADMIN
        );

        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(testAdmin));
        UserPrincipal userPrincipal = appUserDetailsService.loadUserByUsername(adminEmail);

        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        assertThat(authorities).size().isEqualTo(1);
        assertThat(authorities.iterator().next().getAuthority())
                .isEqualTo(PREFIX_ROLE + ROLE_ADMIN);
    }

    @Test
    void loadByUnknownEmailThrowsUsernameNotFoundException() {
        when(userRepository.findByEmail(DEFAULT_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserDetailsService.loadUserByUsername(DEFAULT_EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(DEFAULT_EMAIL);
    }
}
