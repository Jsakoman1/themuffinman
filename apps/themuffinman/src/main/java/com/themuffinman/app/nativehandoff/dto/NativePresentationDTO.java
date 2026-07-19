package com.themuffinman.app.nativehandoff.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NativePresentationDTO {
    private String contractVersion;
    private String targetDevice;
    private String presentationMode;
    private int maxVisibleActions;
    private boolean supportsVoiceInput;
    private boolean supportsTextInput;
    private boolean supportsHapticFeedback;
    private boolean supportsBackgroundRefresh;
    private List<String> allowedCapabilities;
    private String offlinePolicy;
    private String accessibilityPolicy;
}
