package com.themuffinman.app.prompt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromptSemanticPlan {

    private String candidateIntent;
    private Double candidateIntentConfidence;
    private String capabilityId;
    private String planningNote;
    private String searchQuery;

    public static PromptSemanticPlan empty() {
        return PromptSemanticPlan.builder()
                .candidateIntent("UNSUPPORTED")
                .candidateIntentConfidence(0.0d)
                .capabilityId("unsupported")
                .planningNote("")
                .searchQuery("")
                .build();
    }

    public static PromptSemanticPlan createQuest(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("CREATE_QUEST")
                .candidateIntentConfidence(confidence)
                .capabilityId("create_quest")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .build();
    }

    public static PromptSemanticPlan discoverQuests(double confidence, String note, String searchQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("DISCOVER_QUESTS")
                .candidateIntentConfidence(confidence)
                .capabilityId("discover_quests")
                .planningNote(note == null ? "" : note)
                .searchQuery(searchQuery == null ? "" : searchQuery)
                .build();
    }

    public boolean isUnsupported() {
        return candidateIntent == null || candidateIntent.isBlank() || "UNSUPPORTED".equalsIgnoreCase(candidateIntent.trim());
    }

    public String searchQueryOrEmpty() {
        return searchQuery == null ? "" : searchQuery.trim();
    }
}
