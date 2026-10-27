package com.github.interviewbeaterservice.auth;

import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    @Size(min = 32) String secret,
    Duration ttl,
    Duration refreshTtl
) {

}
