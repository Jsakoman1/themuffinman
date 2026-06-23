package com.sidequest.sidequest.dto.auth;

public record AuthResponse(
        Long id,
        String email,
        String username,
        String profileDescription,
        String profileAvatarDataUrl,
        java.time.Instant createdAt,
        String role,
        String token
) {
}
