package com.themuffinman.app.vision.dto;

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
public class DashboardVisionPromptResponseDTO {
    private String prompt;
    private String normalizedPrompt;
    private String source;
    private String translationProvider;
    private String understandingProvider;
    private String understandingStatus;
    private boolean translationApplied;
    private boolean translationReliable;
    private String activeFilter;
    private String surfaceMode;
    private String assistantNote;
    private List<String> matchedSignals;
}
