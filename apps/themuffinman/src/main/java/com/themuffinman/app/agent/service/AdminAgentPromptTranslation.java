package com.themuffinman.app.agent.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAgentPromptTranslation {
    private final String sourceLanguage;
    private final String originalPrompt;
    private final String translatedPrompt;
    private final String translationProvider;
    private final boolean translationApplied;
    private final boolean translationReliable;
}
