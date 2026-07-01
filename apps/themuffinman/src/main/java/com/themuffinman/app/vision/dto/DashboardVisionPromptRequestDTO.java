package com.themuffinman.app.vision.dto;

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
public class DashboardVisionPromptRequestDTO {
    @NotBlank
    private String prompt;

    private String source;
}
