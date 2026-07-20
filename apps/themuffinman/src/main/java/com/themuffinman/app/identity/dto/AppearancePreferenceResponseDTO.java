package com.themuffinman.app.identity.dto;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class AppearancePreferenceResponseDTO {
    private final String theme;
}
