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
    private String targetUserQuery;
    private String targetScope;
    private String selectedCandidateId;
    private Double selectedCandidateConfidence;
    private Boolean clarificationRequired;
    private Boolean broadenSearch;

    public static PromptSemanticPlan empty() {
        return PromptSemanticPlan.builder()
                .candidateIntent("UNSUPPORTED")
                .candidateIntentConfidence(0.0d)
                .capabilityId("unsupported")
                .planningNote("")
                .searchQuery("")
                .targetUserQuery("")
                .targetScope("")
                .selectedCandidateId("")
                .clarificationRequired(false)
                .broadenSearch(false)
                .build();
    }

    public static PromptSemanticPlan createQuest(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("CREATE_QUEST")
                .candidateIntentConfidence(confidence)
                .capabilityId("create_quest")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan createCircle(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("CREATE_CIRCLE")
                .candidateIntentConfidence(confidence)
                .capabilityId("create_circle")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan createCircleRequest(double confidence, String note, String targetUserQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("CREATE_CIRCLE_REQUEST")
                .candidateIntentConfidence(confidence)
                .capabilityId("create_circle_request")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static PromptSemanticPlan acceptCircleRequest(double confidence, String note, String targetUserQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("ACCEPT_CIRCLE_REQUEST")
                .candidateIntentConfidence(confidence)
                .capabilityId("accept_circle_request")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static PromptSemanticPlan deleteCircleRequest(double confidence, String note, String targetUserQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("DELETE_CIRCLE_REQUEST")
                .candidateIntentConfidence(confidence)
                .capabilityId("delete_circle_request")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static PromptSemanticPlan createApplication(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("CREATE_APPLICATION")
                .candidateIntentConfidence(confidence)
                .capabilityId("create_application")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan updateApplication(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("UPDATE_APPLICATION")
                .candidateIntentConfidence(confidence)
                .capabilityId("update_application")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan withdrawApplication(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("WITHDRAW_APPLICATION")
                .candidateIntentConfidence(confidence)
                .capabilityId("withdraw_application")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan approveApplication(double confidence, String note, String targetUserQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("APPROVE_APPLICATION")
                .candidateIntentConfidence(confidence)
                .capabilityId("approve_application")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static PromptSemanticPlan declineApplication(double confidence, String note, String targetUserQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("DECLINE_APPLICATION")
                .candidateIntentConfidence(confidence)
                .capabilityId("decline_application")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static PromptSemanticPlan updateCircle(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("UPDATE_CIRCLE")
                .candidateIntentConfidence(confidence)
                .capabilityId("update_circle")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan deleteCircle(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("DELETE_CIRCLE")
                .candidateIntentConfidence(confidence)
                .capabilityId("delete_circle")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan updateProfile(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("UPDATE_PROFILE")
                .candidateIntentConfidence(confidence)
                .capabilityId("update_profile")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan updateProfileLocation(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("UPDATE_PROFILE_LOCATION")
                .candidateIntentConfidence(confidence)
                .capabilityId("update_profile_location")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan discoverQuests(double confidence, String note, String searchQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("DISCOVER_QUESTS")
                .candidateIntentConfidence(confidence)
                .capabilityId("discover_quests")
                .planningNote(note == null ? "" : note)
                .searchQuery(searchQuery == null ? "" : searchQuery)
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewQuestNews(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_QUEST_NEWS")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_quest_news")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewNotifications(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_NOTIFICATIONS")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_notifications")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan openChat(double confidence, String note, String targetUserQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("OPEN_CHAT")
                .candidateIntentConfidence(confidence)
                .capabilityId("open_chat")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static PromptSemanticPlan sendMessage(double confidence, String note, String targetUserQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("SEND_MESSAGE")
                .candidateIntentConfidence(confidence)
                .capabilityId("send_message")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static PromptSemanticPlan viewChatWorkspace(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_CHAT_WORKSPACE")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_chat_workspace")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewProfile(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_PROFILE")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_profile")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewUserProfile(double confidence, String note, String targetUserQuery) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_USER_PROFILE")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_user_profile")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery(targetUserQuery == null ? "" : targetUserQuery)
                .build();
    }

    public static PromptSemanticPlan viewSettings(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_SETTINGS")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_settings")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewCircles(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_CIRCLES")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_circles")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewCircleDetail(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_CIRCLE_DETAIL")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_circle_detail")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewQuestDetail(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_QUEST_DETAIL")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_quest_detail")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewApplications(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_APPLICATIONS")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_applications")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewMyWork(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_MY_WORK")
                .candidateIntentConfidence(confidence)
                .capabilityId("work.quest.view_own")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public static PromptSemanticPlan viewApplicationDetail(double confidence, String note) {
        return PromptSemanticPlan.builder()
                .candidateIntent("VIEW_APPLICATION_DETAIL")
                .candidateIntentConfidence(confidence)
                .capabilityId("view_application_detail")
                .planningNote(note == null ? "" : note)
                .searchQuery("")
                .targetUserQuery("")
                .build();
    }

    public boolean isUnsupported() {
        return candidateIntent == null || candidateIntent.isBlank() || "UNSUPPORTED".equalsIgnoreCase(candidateIntent.trim());
    }

    public String searchQueryOrEmpty() {
        return searchQuery == null ? "" : searchQuery.trim();
    }
}
