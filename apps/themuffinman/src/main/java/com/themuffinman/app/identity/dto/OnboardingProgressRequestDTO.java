package com.themuffinman.app.identity.dto;

import lombok.Data;

@Data
public class OnboardingProgressRequestDTO {
    private String currentStep;
    private boolean skipped;
    private boolean completed;
}
