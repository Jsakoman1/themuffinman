package com.themuffinman.app.agent.dto;

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
public class AgentResolutionRequirementDTO {
    private String entityType;
    private String workflowId;
    private String scope;
    private String selectionRule;
    private String ambiguityPolicy;
    private String endpointHint;
}
