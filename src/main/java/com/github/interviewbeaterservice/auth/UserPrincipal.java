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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Role role = user.getRole();
        return Collections.singletonList(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

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
