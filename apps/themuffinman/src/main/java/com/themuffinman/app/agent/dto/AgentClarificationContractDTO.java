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
public class AgentClarificationContractDTO {
    private boolean clarificationRequired;
    private boolean failClosedOnAmbiguity;
    private String reason;
    private List<String> unresolvedFields;
}
