package com.themuffinman.app.nativehandoff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NativeHandoffConsumeRequestDTO {
    @NotBlank @Size(max = 256)
    private String token;
    @NotBlank @Size(max = 32)
    private String targetDevice;
}
