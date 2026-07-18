package com.themuffinman.app.identity.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRecoveryRequestDTO(
        @NotBlank @Email @Size(max = 320) String email
) {
}
