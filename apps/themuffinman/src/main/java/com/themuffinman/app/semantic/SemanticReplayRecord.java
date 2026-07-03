package com.themuffinman.app.semantic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticReplayRecord {

    private String contractVersion;
    private String provider;
    private String model;
    private String sourceLanguage;
    private String rawUserText;
    private String normalizedEnglishText;
    private String intent;
    private SemanticEntityFamily entityFamily;
    private String targetEntityQuery;
    private SemanticEntityResolutionStatus entityResolutionStatus;
    private String entityResolutionLabel;
    private Double entityResolutionConfidence;
    private String entityResolutionReason;
    @Builder.Default
    private List<String> requiredSlotIds = List.of();
    @Builder.Default
    private List<String> missingRequiredSlotIds = List.of();
    private boolean clarificationRequired;
    private Double confidence;
    private String ambiguityReason;
    private String capturedAt;
}
