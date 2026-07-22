package com.themuffinman.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponseDTO {
    private String code;
    private String message;
    private List<ApiFieldErrorDTO> fieldErrors;
    private String correlationId;
    private String operationKey;
    private Boolean retryable;
    private String conflictType;
    private String resourceId;
    private String resourceVersion;
}
