package com.moustafa.jobtrackr.auth;

import com.moustafa.jobtrackr.auth.dto.AuthResponse;
import com.moustafa.jobtrackr.auth.dto.LoginRequest;
import com.moustafa.jobtrackr.auth.dto.RegisterRequest;
import com.moustafa.jobtrackr.common.exception.BadRequestException;
import com.moustafa.jobtrackr.security.AuthenticatedUserProvider;
import com.moustafa.jobtrackr.security.JwtService;
import com.moustafa.jobtrackr.user.Role;
import com.moustafa.jobtrackr.user.User;
import com.moustafa.jobtrackr.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticatedUserProvider);
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Moustafa", "MOUSTAFA@EXAMPLE.COM", "password123");
        User savedUser = User.builder()
                .id(1L)
                .fullName("Moustafa")
                .email("moustafa@example.com")
                .passwordHash("encoded-password")
                .role(Role.USER)
                .createdAt(Instant.parse("2026-05-28T05:00:00Z"))
                .build();

        when(userRepository.existsByEmail("moustafa@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("moustafa@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("moustafa@example.com");
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Moustafa", "moustafa@example.com", "password123");

        when(userRepository.existsByEmail("moustafa@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginRejectsInvalidPassword() {
        LoginRequest request = new LoginRequest("moustafa@example.com", "wrong-password");
        User user = User.builder()
                .email("moustafa@example.com")
                .passwordHash("encoded-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("moustafa@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid email or password");

        verify(jwtService, never()).generateToken(any(User.class));
    }
}
