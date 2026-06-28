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
public class AdminAgentSimulationResponseDTO {
    private boolean planningOnly;
    private boolean safeToExecute;
    private String promptSourceLanguage;
    private String translatedPrompt;
    private String selectedIntentId;
    private AgentResolutionConfidenceDTO resolutionConfidence;
    private List<AgentCapabilityAssessmentDTO> capabilityAssessments;
    private AgentIntentLineageDTO intentLineage;
    private List<AgentEndpointPlanDTO> endpointPlan;
    private AgentClarificationContractDTO clarificationContract;
    private AgentExecutionReadinessDTO executionReadiness;
    private List<String> matchedSignals;
    private List<String> unresolvedInputs;
    private List<String> blockingReasons;
    private List<String> suggestedWorkflows;
}
