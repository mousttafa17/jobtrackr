package com.moustafa.jobtrackr.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DefaultUserProvider {

    private static final String DEFAULT_EMAIL = "demo@jobtrackr.com";

    private final UserRepository userRepository;

    @Transactional
    public User getCurrentUser() {
        return userRepository.findByEmail(DEFAULT_EMAIL)
                .orElseGet(this::createDefaultUser);
    }

    private User createDefaultUser() {
        return userRepository.save(User.builder()
                .fullName("Demo User")
                .email(DEFAULT_EMAIL)
                .passwordHash("temporary-password-hash")
                .role(Role.USER)
                .build());
    }
}
