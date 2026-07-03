package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionMemoryTrailDTO {
    private String activeEntityFamily;
    private String previousEntityFamily;
    private String topicSwitchHint;
    private String currentIntent;
    private String currentRequestedSlot;
    private String currentStatus;
    private String sessionSummary;
    private String lastUserPrompt;
    private String lastNormalizedPrompt;
    private String lastAssistantMessage;
    private String sessionMemorySnapshot;
    private List<String> openQuestions;
    private List<String> recentActions;
    private List<String> recentEntityFamilies;
    private List<String> recentIntentTypes;
}
