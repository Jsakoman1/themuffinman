package com.themuffinman.app.identity.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(max = 100) String password
) {
}
