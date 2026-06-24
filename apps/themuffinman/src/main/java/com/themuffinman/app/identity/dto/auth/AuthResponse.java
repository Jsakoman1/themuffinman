package com.themuffinman.app.identity.dto.auth;

import org.springframework.lang.Nullable;

public record AuthResponse(
        Long id,
        String email,
        String username,
        @Nullable
        String profileDescription,
        @Nullable
        String profileAvatarDataUrl,
        java.time.Instant createdAt,
        String role,
        @Nullable
        String token
) {
}
