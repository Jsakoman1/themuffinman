package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.themuffinman.app.prompt.PromptSemanticPlan;
import com.themuffinman.app.vision.model.VisionIntent;
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
public class VisionSemanticPlan {

    private String candidateIntent;
    private Double candidateIntentConfidence;
    private String capabilityId;
    private String planningNote;
    private String searchQuery;
    private String targetUserQuery;

    public static VisionSemanticPlan empty() {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.UNSUPPORTED.name())
                .candidateIntentConfidence(0.0d)
                .capabilityId("unsupported")
                .planningNote("")
                .build();
    }

    public static VisionSemanticPlan createQuest(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.CREATE_QUEST.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("create_quest")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan discoverQuests(double confidence, String note, String searchQuery) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.DISCOVER_QUESTS.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("discover_quests")
                .planningNote(note == null ? "" : note)
                .searchQuery(searchQuery == null ? "" : searchQuery)
                .build();
    }

    public static VisionSemanticPlan from(PromptSemanticPlan plan) {
        if (plan == null) {
            return empty();
        }
        return VisionSemanticPlan.builder()
                .candidateIntent(plan.getCandidateIntent())
                .candidateIntentConfidence(plan.getCandidateIntentConfidence())
                .capabilityId(plan.getCapabilityId())
                .planningNote(plan.getPlanningNote())
                .searchQuery(plan.getSearchQuery())
                .build();
    }

    public VisionIntent candidateIntentOrUnsupported() {
        if (candidateIntent == null || candidateIntent.isBlank()) {
            return VisionIntent.UNSUPPORTED;
        }
        try {
            return VisionIntent.valueOf(candidateIntent.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return VisionIntent.UNSUPPORTED;
        }
    }

    public String searchQueryOrEmpty() {
        return searchQuery == null ? "" : searchQuery.trim();
    }
}
