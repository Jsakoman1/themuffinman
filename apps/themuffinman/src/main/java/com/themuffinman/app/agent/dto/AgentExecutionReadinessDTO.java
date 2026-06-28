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
public class AgentExecutionReadinessDTO {
    private boolean planningOnly;
    private boolean translationReady;
    private boolean requiresExternalTranslationProvider;
    private boolean currentLocationCapabilityRequired;
    private String currentLocationCapabilityStatus;
    private boolean destructiveConfirmationRequired;
    private boolean multiActorContextRequired;
}
