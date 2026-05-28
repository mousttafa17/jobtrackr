package com.moustafa.jobtrackr.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moustafa.jobtrackr.user.Role;
import com.moustafa.jobtrackr.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            new ObjectMapper(),
            "test-secret-that-is-long-enough-for-local-tests",
            60
    );

    @Test
    void generatedTokenCanBeValidatedForUser() {
        User user = User.builder()
                .id(1L)
                .fullName("Moustafa")
                .email("moustafa@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .createdAt(Instant.now())
                .build();
        UserDetails userDetails = new UserPrincipal(user);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractEmail(token)).isEqualTo("moustafa@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void tamperedTokenIsInvalid() {
        User user = User.builder()
                .id(1L)
                .email("moustafa@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .build();
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token + "tampered", new UserPrincipal(user))).isFalse();
    }
}
