package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.user.entity.Role;
import com.github.interviewbeaterservice.user.entity.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    public static final String ROLE_PREFIX = "ROLE_";

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    /**
     * Роль пользователя в виде authority {@code ROLE_ + Role#name()}.
     * @return Список с authority пользователя
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = user.getRole();
        return Collections.singletonList(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()));
    }

    /**
     * Пароль пользователя (BCrypt-хеш).
     * @return хеш пароля
     */
    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    /**
     * Username в терминах Spring Security — email пользователя.
     * @return email
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Получить id пользователя
     * @return возвращает id текущего пользователя
     */
    public Long getUserId() {
        return user.getId();
    }
}
