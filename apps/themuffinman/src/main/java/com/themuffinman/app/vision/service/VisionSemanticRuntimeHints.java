package com.themuffinman.app.vision.service;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VisionSemanticRuntimeHints {
    private String inputType;
    private String clientLocale;
    private String clientTimezone;
    private List<String> clientCapabilities;
    private String clientStateVersion;
}
