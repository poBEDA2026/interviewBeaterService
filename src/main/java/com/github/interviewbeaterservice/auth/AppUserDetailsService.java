package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format("Пользователь с email %s не найден", username)));

        return new UserPrincipal(user);
    }

    /**
     * Получить пользователя по id
     * @param userId id пользователя
     * @return пользователь
     */
    public UserPrincipal loadUserById(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new BadCredentialsException(String.format("Пользователь с id %d не найден", userId)));

        return new UserPrincipal(user);
    }
}
