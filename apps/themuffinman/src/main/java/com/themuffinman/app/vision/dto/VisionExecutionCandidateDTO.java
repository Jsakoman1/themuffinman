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
public class VisionExecutionCandidateDTO {
    private String candidateIntent;
    private String capabilityId;
    private Double confidence;
    private boolean reviewReady;
    private boolean executionReady;
    private boolean confirmationRequired;
    private String nextRequiredSlot;
    private String blockingReason;
    private String failureCode;
    private boolean retryable;
    private String planningNote;
    private String summary;
}
