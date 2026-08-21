package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessResourceRequirementRequestDTO {
    @NotNull
    private Long businessOfferingId;
    private Long resourcePoolId;
    @NotBlank @Size(max = 40)
    private String resourceType;
    @Min(1)
    private Integer requiredCount;
    @NotBlank @Size(max = 32)
    private String assignmentMode;
}
