package com.themuffinman.app.vision.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter @Setter
public class GuidedIntakeRequestDTO {
    @NotBlank private String flow;
    private Map<String, String> draft = new LinkedHashMap<>();
    private String fieldId;
    private String fieldValue;
    private String action;
}
