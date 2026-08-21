package com.themuffinman.app.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class BusinessResourceRequestDTO {
    private Long resourcePoolId;
    @NotBlank @Size(max = 80)
    private String resourceKey;
    @NotBlank @Size(max = 160)
    private String label;
    @NotBlank @Size(max = 40)
    private String resourceType;
    @Size(max = 160)
    private String publicLabel;
    private Boolean active;
    private Map<String, Object> metadata;
}
