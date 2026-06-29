package com.themuffinman.app.identity.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
