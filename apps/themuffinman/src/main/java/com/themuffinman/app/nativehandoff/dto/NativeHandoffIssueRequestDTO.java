package com.themuffinman.app.nativehandoff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NativeHandoffIssueRequestDTO {
    @NotBlank @Size(max = 32)
    private String targetDevice;
    @NotBlank @Size(max = 120)
    private String intent;
    @Size(max = 160)
    private String resourceReference;
    @Size(max = 20000)
    private String redactedContext;
}
