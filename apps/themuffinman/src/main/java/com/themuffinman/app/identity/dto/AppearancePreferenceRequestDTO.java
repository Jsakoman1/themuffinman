package com.themuffinman.app.identity.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AppearancePreferenceRequestDTO {
    @NotBlank
    @Pattern(regexp = "SYSTEM|DARK|LIGHT")
    private String theme;
}
