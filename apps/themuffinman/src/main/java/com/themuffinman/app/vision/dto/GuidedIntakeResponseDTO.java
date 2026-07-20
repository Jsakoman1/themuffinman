package com.themuffinman.app.vision.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter @Builder
public class GuidedIntakeResponseDTO {
    private final String flow;
    private final GuidedIntakeStepDTO step;
    private final Map<String, String> draft;
    private final boolean reviewReady;
}
