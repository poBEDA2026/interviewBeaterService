package com.github.interviewbeaterservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/swagger-ui.html",
                            "/api/swagger-ui/**",
                            "/api/docs",
                            "/api/docs/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/questions/*/attachments", "/questions/*/attachments/*").permitAll()
                    .requestMatchers(HttpMethod.POST, "/questions/*/attachments").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/questions/*/attachments/*").authenticated()
                    .anyRequest().permitAll()
            );
        return http.build();
    }
}