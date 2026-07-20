package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
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
    private String targetScope;
    private String selectedCandidateId;
    private Double selectedCandidateConfidence;
    private Boolean clarificationRequired;
    private Boolean broadenSearch;

    public static VisionSemanticPlan empty() {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.UNSUPPORTED.name())
                .candidateIntentConfidence(0.0d)
                .capabilityId("unsupported")
                .planningNote("")
                .targetScope("")
                .selectedCandidateId("")
                .clarificationRequired(false)
                .broadenSearch(false)
                .build();
    }

    public String selectedCandidateIdOrEmpty() {
        return selectedCandidateId == null ? "" : selectedCandidateId.trim();
    }

    public boolean requiresCandidateClarification() {
        return Boolean.TRUE.equals(clarificationRequired);
    }

    public static VisionSemanticPlan createQuest(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.CREATE_QUEST.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("create_quest")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan createCircle(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.CREATE_CIRCLE.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("create_circle")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan createCircleRequest(double confidence, String note, String targetUserQuery) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.CREATE_CIRCLE_REQUEST.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("create_circle_request")
                .planningNote(note == null ? "" : note)
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static VisionSemanticPlan acceptCircleRequest(double confidence, String note, String targetUserQuery) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.ACCEPT_CIRCLE_REQUEST.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("accept_circle_request")
                .planningNote(note == null ? "" : note)
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static VisionSemanticPlan deleteCircleRequest(double confidence, String note, String targetUserQuery) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.DELETE_CIRCLE_REQUEST.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("delete_circle_request")
                .planningNote(note == null ? "" : note)
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static VisionSemanticPlan createApplication(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.CREATE_APPLICATION.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("create_application")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan updateApplication(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.UPDATE_APPLICATION.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("update_application")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan withdrawApplication(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.WITHDRAW_APPLICATION.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("withdraw_application")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan approveApplication(double confidence, String note, String targetUserQuery) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.APPROVE_APPLICATION.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("approve_application")
                .planningNote(note == null ? "" : note)
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static VisionSemanticPlan declineApplication(double confidence, String note, String targetUserQuery) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.DECLINE_APPLICATION.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("decline_application")
                .planningNote(note == null ? "" : note)
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static VisionSemanticPlan updateCircle(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.UPDATE_CIRCLE.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("update_circle")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan deleteCircle(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.DELETE_CIRCLE.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("delete_circle")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan updateProfile(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.UPDATE_PROFILE.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("update_profile")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan updateProfileLocation(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.UPDATE_PROFILE_LOCATION.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("update_profile_location")
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

    public static VisionSemanticPlan openChat(double confidence, String note, String targetUserQuery) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.OPEN_CHAT.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("open_chat")
                .planningNote(note == null ? "" : note)
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static VisionSemanticPlan viewChatWorkspace(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_CHAT_WORKSPACE.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_chat_workspace")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan viewProfile(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_PROFILE.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_profile")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan viewUserProfile(double confidence, String note, String targetUserQuery) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_USER_PROFILE.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_user_profile")
                .planningNote(note == null ? "" : note)
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static VisionSemanticPlan viewSettings(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_SETTINGS.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_settings")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan viewCircles(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_CIRCLES.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_circles")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan viewCircleDetail(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_CIRCLE_DETAIL.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_circle_detail")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan viewQuestDetail(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_QUEST_DETAIL.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_quest_detail")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan viewApplications(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_APPLICATIONS.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_applications")
                .planningNote(note == null ? "" : note)
                .build();
    }

    public static VisionSemanticPlan viewApplicationDetail(double confidence, String note) {
        return VisionSemanticPlan.builder()
                .candidateIntent(VisionIntent.VIEW_APPLICATION_DETAIL.name())
                .candidateIntentConfidence(confidence)
                .capabilityId("view_application_detail")
                .planningNote(note == null ? "" : note)
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
                .targetUserQuery(plan.getTargetUserQuery())
                .targetScope(plan.getTargetScope())
                .selectedCandidateId(plan.getSelectedCandidateId())
                .selectedCandidateConfidence(plan.getSelectedCandidateConfidence())
                .clarificationRequired(plan.getClarificationRequired())
                .broadenSearch(plan.getBroadenSearch())
                .build();
    }

    public VisionIntent candidateIntentOrUnsupported() {
        if (candidateIntent == null || candidateIntent.isBlank()) {
            return VisionIntent.UNSUPPORTED;
        }
        try {
            return VisionIntent.valueOf(TextValueNormalizer.upperTrimToEmpty(candidateIntent));
        } catch (IllegalArgumentException ignored) {
            return VisionIntent.UNSUPPORTED;
        }
    }

    public String searchQueryOrEmpty() {
        return searchQuery == null ? "" : searchQuery.trim();
    }
}
