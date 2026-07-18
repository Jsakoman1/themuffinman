package com.themuffinman.app.identity.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequestDTO(
        @NotBlank @Size(max = 256) String token,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
