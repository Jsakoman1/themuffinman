package com.themuffinman.app.identity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WorkspaceRailPreferenceRequestDTO {
    @Min(216) @Max(280) private int railWidthPx;
}
