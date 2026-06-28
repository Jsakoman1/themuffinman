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
public class AgentIntentLineageDTO {
    private String intentId;
    private List<String> sourcePromptExamples;
    private List<String> resolutionWorkflows;
    private List<String> targetEndpoints;
    private List<String> safetyPolicies;
    private List<String> expectedReadModels;
}
