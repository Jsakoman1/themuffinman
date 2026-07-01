package com.themuffinman.app.agent.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AdminAgentExecutionRequestDTO {
    @NotBlank(message = "Prompt is required")
    private String prompt;
    private boolean confirmed;
}
