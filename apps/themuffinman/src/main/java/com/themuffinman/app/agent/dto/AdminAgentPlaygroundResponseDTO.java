package com.themuffinman.app.agent.dto;

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
public class AdminAgentPlaygroundResponseDTO {
    private String provider;
    private boolean externalLlmConfigured;
    private String promptSourceLanguage;
    private String promptTranslationProvider;
    private boolean promptTranslationApplied;
    private boolean promptTranslationReliable;
    private String originalPrompt;
    private String translatedPrompt;
    private String title;
    private String summary;
    private List<AgentResolutionRequirementDTO> resolutionRequirements;
    private AgentClarificationContractDTO clarificationContract;
    private AgentExecutionReadinessDTO executionReadiness;
    private List<String> matchedSignals;
    private List<String> unresolvedInputs;
    private List<String> warnings;
    private List<String> suggestedWorkflows;
    private List<String> nextSteps;
    private boolean directExecutionAvailable;
    private String directExecutionCapabilityId;
    private String directExecutionSummary;
}
