package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionLearningExplainabilityDTO {
    private String decisionType;
    private String preferenceKey;
    private String preferenceValue;
    private int rank;
    private Double confidenceScore;
    private int observationCount;
    private String sourceType;
    private String reason;
}
