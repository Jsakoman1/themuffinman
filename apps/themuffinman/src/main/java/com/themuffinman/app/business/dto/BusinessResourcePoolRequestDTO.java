package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BusinessResourcePoolRequestDTO {
    @NotBlank @Size(max = 80)
    private String poolKey;
    @NotBlank @Size(max = 160)
    private String label;
    @NotBlank @Size(max = 40)
    private String resourceType;
    @Min(1)
    private Integer capacity;
    @Size(max = 160)
    private String publicLabel;
    private Boolean active;
}
