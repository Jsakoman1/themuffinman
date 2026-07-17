package com.themuffinman.app.nativehandoff.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class NativeHandoffIssueResponseDTO {
    private String token;
    private String targetDevice;
    private String intent;
    private String resourceReference;
    private Instant expiresAt;
    private String contractVersion;
}
