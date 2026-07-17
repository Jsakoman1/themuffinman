package com.themuffinman.app.identity.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter @Builder
public class OnboardingProgressResponseDTO {
    private Long id;
    private String currentStep;
    private boolean skipped;
    private boolean completed;
    private Instant updatedAt;
}
