package com.themuffinman.app.vision.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter @Builder
public class GuidedIntakeStepDTO {
    private final String fieldId;
    private final String inputKind;
    private final String label;
    private final String placeholder;
    private final List<String> choices;
    private final String currentValue;
    private final boolean valid;
    private final String error;
    private final String nextAction;
    private final boolean complete;
}
