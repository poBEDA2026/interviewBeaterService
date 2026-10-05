package com.github.interviewbeaterservice.auth;

import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AppUserDetailsService implements UserDetailsService {

    public final UserRepository userRepository;

    AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format("Пользователь с email %s не найден", username)));

        return new UserPrincipal(user);
    }
}
