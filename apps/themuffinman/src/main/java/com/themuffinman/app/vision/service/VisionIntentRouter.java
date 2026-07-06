package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class VisionIntentRouter {

    private final VisionProperties visionProperties;
    private final VisionSemanticRouteCatalogService semanticRouteCatalogService;
    private final VisionIntentSignalSupport visionIntentSignalSupport;

    public VisionIntentRouter(VisionProperties visionProperties, VisionSemanticRouteCatalogService semanticRouteCatalogService) {
        this.visionProperties = visionProperties;
        this.semanticRouteCatalogService = semanticRouteCatalogService;
        this.visionIntentSignalSupport = new VisionIntentSignalSupport();
    }

    public VisionIntent detectIntent(String prompt) {
        return detectIntent(prompt, null);
    }

    public VisionIntent detectIntent(String prompt, VisionPromptUnderstandingResult understanding) {
        String lower = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        VisionIntent semanticIntent = understanding == null
                ? VisionIntent.UNSUPPORTED
                : understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported();
        VisionIntent snapshotOverride = visionIntentSignalSupport.overrideSnapshotIntent(semanticIntent, lower);
        if (snapshotOverride != null) {
            return snapshotOverride;
        }
        if (semanticIntent == VisionIntent.DISCOVER_QUESTS) {
            return VisionIntent.DISCOVER_QUESTS;
        }
        if (semanticIntent == VisionIntent.SEARCH) {
            return VisionIntent.SEARCH;
        }
        if (semanticIntent == VisionIntent.CREATE_CIRCLE) {
            return VisionIntent.CREATE_CIRCLE;
        }
        if (semanticIntent == VisionIntent.CREATE_CIRCLE_REQUEST) {
            return VisionIntent.CREATE_CIRCLE_REQUEST;
        }
        if (semanticIntent == VisionIntent.ACCEPT_CIRCLE_REQUEST) {
            return VisionIntent.ACCEPT_CIRCLE_REQUEST;
        }
        if (semanticIntent == VisionIntent.DELETE_CIRCLE_REQUEST) {
            return VisionIntent.DELETE_CIRCLE_REQUEST;
        }
        if (semanticIntent == VisionIntent.CREATE_APPLICATION) {
            return VisionIntent.CREATE_APPLICATION;
        }
        if (semanticIntent == VisionIntent.UPDATE_APPLICATION) {
            return VisionIntent.UPDATE_APPLICATION;
        }
        if (semanticIntent == VisionIntent.WITHDRAW_APPLICATION) {
            return VisionIntent.WITHDRAW_APPLICATION;
        }
        if (semanticIntent == VisionIntent.APPROVE_APPLICATION) {
            return VisionIntent.APPROVE_APPLICATION;
        }
        if (semanticIntent == VisionIntent.DECLINE_APPLICATION) {
            return VisionIntent.DECLINE_APPLICATION;
        }
        if (semanticIntent == VisionIntent.UPDATE_CIRCLE) {
            return VisionIntent.UPDATE_CIRCLE;
        }
        if (semanticIntent == VisionIntent.DELETE_CIRCLE) {
            return VisionIntent.DELETE_CIRCLE;
        }
        if (semanticIntent == VisionIntent.UPDATE_PROFILE) {
            return VisionIntent.UPDATE_PROFILE;
        }
        if (semanticIntent == VisionIntent.UPDATE_PROFILE_LOCATION) {
            return VisionIntent.UPDATE_PROFILE_LOCATION;
        }
        if (semanticIntent == VisionIntent.OPEN_CHAT) {
            return VisionIntent.OPEN_CHAT;
        }
        if (semanticIntent == VisionIntent.VIEW_CHAT_WORKSPACE) {
            return VisionIntent.VIEW_CHAT_WORKSPACE;
        }
        if (semanticIntent == VisionIntent.VIEW_SETTINGS) {
            return VisionIntent.VIEW_SETTINGS;
        }
        if (semanticIntent == VisionIntent.VIEW_USER_PROFILE) {
            return VisionIntent.VIEW_USER_PROFILE;
        }
        if (semanticIntent == VisionIntent.VIEW_PROFILE) {
            return VisionIntent.VIEW_PROFILE;
        }
        if (semanticIntent == VisionIntent.VIEW_CIRCLE_DETAIL) {
            return VisionIntent.VIEW_CIRCLE_DETAIL;
        }
        if (semanticIntent == VisionIntent.VIEW_QUEST_DETAIL) {
            return VisionIntent.VIEW_QUEST_DETAIL;
        }
        if (semanticIntent == VisionIntent.VIEW_NOTIFICATIONS) {
            return VisionIntent.VIEW_NOTIFICATIONS;
        }
        if (semanticIntent == VisionIntent.VIEW_QUEST_NEWS) {
            return VisionIntent.VIEW_QUEST_NEWS;
        }
        if (semanticIntent == VisionIntent.VIEW_CIRCLES) {
            return VisionIntent.VIEW_CIRCLES;
        }
        if (semanticIntent == VisionIntent.VIEW_APPLICATION_DETAIL) {
            return VisionIntent.VIEW_APPLICATION_DETAIL;
        }
        if (semanticIntent == VisionIntent.VIEW_APPLICATIONS) {
            return VisionIntent.VIEW_APPLICATIONS;
        }
        if (semanticRouteCatalogService.routeForIntent(semanticIntent.name()) != null) {
            return semanticIntent;
        }
        if (visionIntentSignalSupport.containsCircleCreateSignals(lower)) {
            return VisionIntent.CREATE_CIRCLE;
        }
        if (visionIntentSignalSupport.containsCircleRequestAcceptSignals(lower)) {
            return VisionIntent.ACCEPT_CIRCLE_REQUEST;
        }
        if (visionIntentSignalSupport.containsCircleRequestDeleteSignals(lower)) {
            return VisionIntent.DELETE_CIRCLE_REQUEST;
        }
        if (visionIntentSignalSupport.containsCircleRequestCreateSignals(lower)) {
            return VisionIntent.CREATE_CIRCLE_REQUEST;
        }
        if (visionIntentSignalSupport.containsCircleDeleteSignals(lower)) {
            return VisionIntent.DELETE_CIRCLE;
        }
        if (visionIntentSignalSupport.containsCircleUpdateSignals(lower)) {
            return VisionIntent.UPDATE_CIRCLE;
        }
        if (visionIntentSignalSupport.containsApplicationWithdrawSignals(lower)) {
            return VisionIntent.WITHDRAW_APPLICATION;
        }
        if (visionIntentSignalSupport.containsApplicationUpdateSignals(lower)) {
            return VisionIntent.UPDATE_APPLICATION;
        }
        if (visionIntentSignalSupport.containsApplicationApproveSignals(lower)) {
            return VisionIntent.APPROVE_APPLICATION;
        }
        if (visionIntentSignalSupport.containsApplicationDeclineSignals(lower)) {
            return VisionIntent.DECLINE_APPLICATION;
        }
        if (visionIntentSignalSupport.containsProfileLocationUpdateSignals(lower)) {
            return VisionIntent.UPDATE_PROFILE_LOCATION;
        }
        if (visionIntentSignalSupport.containsApplicationCreateSignals(lower)) {
            return VisionIntent.CREATE_APPLICATION;
        }
        if (visionIntentSignalSupport.containsProfileUpdateSignals(lower)) {
            return VisionIntent.UPDATE_PROFILE;
        }
        if (visionIntentSignalSupport.containsSettingsSignals(lower)) {
            return VisionIntent.VIEW_SETTINGS;
        }
        if (visionIntentSignalSupport.containsUserProfileDetailSignals(lower)) {
            return VisionIntent.VIEW_USER_PROFILE;
        }
        if (visionIntentSignalSupport.containsCircleDetailSignals(lower)) {
            return VisionIntent.VIEW_CIRCLE_DETAIL;
        }
        if (visionIntentSignalSupport.containsQuestDetailSignals(lower)) {
            return VisionIntent.VIEW_QUEST_DETAIL;
        }
        if (visionIntentSignalSupport.containsNotificationsSignals(lower)) {
            return VisionIntent.VIEW_NOTIFICATIONS;
        }
        if (visionIntentSignalSupport.containsQuestNewsSignals(lower)) {
            return VisionIntent.VIEW_QUEST_NEWS;
        }
        if (visionIntentSignalSupport.containsProfileSignals(lower)) {
            return VisionIntent.VIEW_PROFILE;
        }
        if (visionIntentSignalSupport.containsCirclesSignals(lower)) {
            return VisionIntent.VIEW_CIRCLES;
        }
        if (visionIntentSignalSupport.containsApplicationDetailSignals(lower)) {
            return VisionIntent.VIEW_APPLICATION_DETAIL;
        }
        if (visionIntentSignalSupport.containsApplicationsSignals(lower)) {
            return VisionIntent.VIEW_APPLICATIONS;
        }
        if (visionIntentSignalSupport.containsThingsSignals(lower)) {
            return VisionIntent.VIEW_THINGS;
        }
        if (visionIntentSignalSupport.containsSearchSignals(lower)) {
            return VisionIntent.SEARCH;
        }
        if (visionIntentSignalSupport.containsDiscoverySignals(lower)) {
            return VisionIntent.DISCOVER_QUESTS;
        }
        if (visionIntentSignalSupport.containsChatWorkspaceSignals(lower)) {
            return VisionIntent.VIEW_CHAT_WORKSPACE;
        }
        if (visionIntentSignalSupport.containsChatSignals(lower)) {
            return VisionIntent.OPEN_CHAT;
        }
        if (!visionProperties.isCreateQuestEnabled()) {
            return VisionIntent.UNSUPPORTED;
        }
        if (semanticIntent == VisionIntent.CREATE_QUEST) {
            return VisionIntent.CREATE_QUEST;
        }
        if (lower.contains("create") && lower.contains("quest")) {
            return VisionIntent.CREATE_QUEST;
        }
        if (lower.contains("post") && lower.contains("quest")) {
            return VisionIntent.CREATE_QUEST;
        }
        if (visionIntentSignalSupport.containsAny(lower,
                "create quest",
                "new quest",
                "post a quest",
                "post quest",
                "need someone",
                "looking for someone",
                "i need help",
                "can someone",
                "task for someone",
                "help me with")) {
            return VisionIntent.CREATE_QUEST;
        }
        return VisionIntent.UNSUPPORTED;
    }

}
