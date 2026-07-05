package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionLearningPreferenceDTO {
    private String preferenceKey;
    private String preferenceValue;
    private String sourceType;
    private int observationCount;
    private Double confidenceScore;
    private Instant lastObservedAt;
}
