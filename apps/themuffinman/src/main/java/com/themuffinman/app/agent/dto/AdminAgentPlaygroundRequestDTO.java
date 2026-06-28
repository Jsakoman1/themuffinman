package com.themuffinman.app.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class AdminAgentPlaygroundRequestDTO {

    @NotBlank(message = "Prompt is required")
    @Size(max = 4000, message = "Prompt must be 4000 characters or less")
    private String prompt;
}
