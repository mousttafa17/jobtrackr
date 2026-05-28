package com.moustafa.jobtrackr.auth.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
