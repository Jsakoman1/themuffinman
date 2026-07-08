package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
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
        String lower = TextValueNormalizer.lowerTrimToEmpty(prompt);
        VisionIntent semanticIntent = understanding == null
                ? VisionIntent.UNSUPPORTED
                : understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported();
        VisionIntent snapshotOverride = visionIntentSignalSupport.overrideSnapshotIntent(semanticIntent, lower);
        if (snapshotOverride != null) {
            return snapshotOverride;
        }
        VisionIntent routedSemanticIntent = routeSemanticIntent(semanticIntent);
        if (routedSemanticIntent != null) {
            return routedSemanticIntent;
        }
        if (semanticRouteCatalogService.routeForIntent(semanticIntent.name()) != null) {
            return semanticIntent;
        }
        VisionIntent signalIntent = routeSignalIntent(lower);
        if (signalIntent != null) {
            return signalIntent;
        }
        if (!visionProperties.isCreateQuestEnabled()) {
            return VisionIntent.UNSUPPORTED;
        }
        if (semanticIntent == VisionIntent.CREATE_QUEST
                || lower.contains("create") && lower.contains("quest")
                || lower.contains("post") && lower.contains("quest")
                || visionIntentSignalSupport.containsAny(lower,
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

    private VisionIntent routeSemanticIntent(VisionIntent semanticIntent) {
        return switch (semanticIntent) {
            case DISCOVER_QUESTS,
                 SEARCH,
                 CREATE_CIRCLE,
                 CREATE_CIRCLE_REQUEST,
                 ACCEPT_CIRCLE_REQUEST,
                 DELETE_CIRCLE_REQUEST,
                 CREATE_APPLICATION,
                 UPDATE_APPLICATION,
                 WITHDRAW_APPLICATION,
                 APPROVE_APPLICATION,
                 DECLINE_APPLICATION,
                 UPDATE_CIRCLE,
                 DELETE_CIRCLE,
                 UPDATE_PROFILE,
                 UPDATE_PROFILE_LOCATION,
                 OPEN_CHAT,
                 VIEW_CHAT_WORKSPACE,
                 VIEW_SETTINGS,
                 VIEW_USER_PROFILE,
                 VIEW_PROFILE,
                 VIEW_BUSINESS,
                 VIEW_BUSINESS_AVAILABILITY,
                 VIEW_CIRCLE_DETAIL,
                 VIEW_QUEST_DETAIL,
                 VIEW_NOTIFICATIONS,
                 VIEW_QUEST_NEWS,
                 VIEW_CIRCLES,
                 VIEW_APPLICATION_DETAIL,
                 VIEW_APPLICATIONS -> semanticIntent;
            default -> null;
        };
    }

    private VisionIntent routeSignalIntent(String lower) {
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
        if (visionIntentSignalSupport.containsBusinessAvailabilitySignals(lower)) {
            return VisionIntent.VIEW_BUSINESS_AVAILABILITY;
        }
        if (visionIntentSignalSupport.containsBusinessPageSignals(lower)) {
            return VisionIntent.VIEW_BUSINESS;
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
        return null;
    }

}
