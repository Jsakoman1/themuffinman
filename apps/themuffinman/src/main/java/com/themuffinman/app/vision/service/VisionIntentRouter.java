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
                 SYNC_CHAT,
                 VIEW_CHAT_ATTACHMENT,
                 MARK_CHAT_READ,
                 MARK_NOTIFICATIONS_READ,
                 MARK_NOTIFICATION_READ,
                 UPDATE_NOTIFICATION_PREFERENCES,
                 RELEASE_WORKER,
                 REPLACE_WORKER,
                 REOPEN_QUEST,
                 CANCEL_QUEST,
                 PAUSE_QUEST,
                 RESUME_QUEST,
                 RESCHEDULE_BOOKING,
                 CREATE_THING,
                 REQUEST_BORROW,
                 CANCEL_BORROW,
                 DECIDE_BORROW,
                 RETURN_BORROW,
                 CREATE_RIDE,
                 VIEW_RIDES,
                 JOIN_RIDE,
                 UPDATE_RIDE,
                 LEAVE_RIDE,
                 CANCEL_RIDE,
                 START_RIDE,
                 COMPLETE_RIDE,
                 VIEW_SETTINGS,
                 VIEW_USER_PROFILE,
                 VIEW_PROFILE,
                 VIEW_BUSINESS,
                 VIEW_BUSINESS_AVAILABILITY,
                 VIEW_BUSINESS_BOOKINGS,
                 VIEW_CIRCLE_DETAIL,
                 VIEW_QUEST_DETAIL,
                 VIEW_THING_DETAIL,
                 VIEW_NOTIFICATIONS,
                 VIEW_ACTIVITY,
                 VIEW_QUEST_NEWS,
                 VIEW_CIRCLES,
                 VIEW_APPLICATION_DETAIL,
                 VIEW_APPLICATIONS -> semanticIntent;
            default -> null;
        };
    }

    private VisionIntent routeSignalIntent(String lower) {
        if (lower.contains("ride") || lower.contains("carpool") || lower.contains("lift") || lower.contains("passenger")) {
            if (lower.contains("join") || lower.contains("take me") || lower.contains("reserve")) return VisionIntent.JOIN_RIDE;
            if (lower.contains("leave") || lower.contains("get off")) return VisionIntent.LEAVE_RIDE;
            if (lower.contains("cancel")) return VisionIntent.CANCEL_RIDE;
            if (lower.contains("complete") || lower.contains("finished")) return VisionIntent.COMPLETE_RIDE;
            if (lower.contains("start") || lower.contains("depart")) return VisionIntent.START_RIDE;
            if (lower.contains("update") || lower.contains("change")) return VisionIntent.UPDATE_RIDE;
            if (lower.contains("offer") || lower.contains("create") || lower.contains("drive")) return VisionIntent.CREATE_RIDE;
            return VisionIntent.VIEW_RIDES;
        }
        if (visionIntentSignalSupport.containsNotificationPreferenceSignals(lower)) {
            return VisionIntent.UPDATE_NOTIFICATION_PREFERENCES;
        }
        if (visionIntentSignalSupport.containsWorkerReplacementSignals(lower)) return VisionIntent.REPLACE_WORKER;
        if (visionIntentSignalSupport.containsWorkerReleaseSignals(lower)) return VisionIntent.RELEASE_WORKER;
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
        if (visionIntentSignalSupport.containsCircleLeaveSignals(lower)) return VisionIntent.LEAVE_CIRCLE;
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
        if (visionIntentSignalSupport.containsThingDetailSignals(lower)) {
            return VisionIntent.VIEW_THING_DETAIL;
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
        if (visionIntentSignalSupport.containsBookingRescheduleSignals(lower)) return VisionIntent.RESCHEDULE_BOOKING;
        if (visionIntentSignalSupport.containsBusinessBookingSignals(lower)) {
            return VisionIntent.VIEW_BUSINESS_BOOKINGS;
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
        if (visionIntentSignalSupport.containsQuestPauseSignals(lower)) {
            return lower.contains("resume") || lower.contains("unpause") ? VisionIntent.RESUME_QUEST : VisionIntent.PAUSE_QUEST;
        }
        if (visionIntentSignalSupport.containsQuestCancelSignals(lower)) {
            return VisionIntent.CANCEL_QUEST;
        }
        if (visionIntentSignalSupport.containsApplicationsSignals(lower)) {
            return VisionIntent.VIEW_APPLICATIONS;
        }
        if (visionIntentSignalSupport.containsBorrowRequestSignals(lower)) {
            return VisionIntent.VIEW_BORROW_REQUESTS;
        }
        if (visionIntentSignalSupport.containsChatMessageEditSignals(lower)) {
            return VisionIntent.EDIT_CHAT_MESSAGE;
        }
        if (visionIntentSignalSupport.containsChatMessageReplySignals(lower)) {
            return VisionIntent.REPLY_TO_CHAT_MESSAGE;
        }
        if (visionIntentSignalSupport.containsChatMessageReactionSignals(lower)) {
            return VisionIntent.REACT_TO_CHAT_MESSAGE;
        }
        if (visionIntentSignalSupport.containsBusinessProfileCreateSignals(lower)) {
            return VisionIntent.CREATE_BUSINESS_PROFILE;
        }
        if (visionIntentSignalSupport.containsBusinessProfileUpdateSignals(lower)) {
            return VisionIntent.UPDATE_BUSINESS_PROFILE;
        }
        if (visionIntentSignalSupport.containsGalleryCreateSignals(lower)) return VisionIntent.CREATE_GALLERY_IMAGE;
        if (visionIntentSignalSupport.containsGalleryUpdateSignals(lower)) return VisionIntent.UPDATE_GALLERY_IMAGE;
        if (visionIntentSignalSupport.containsGalleryDeleteSignals(lower)) return VisionIntent.DELETE_GALLERY_IMAGE;
        if (visionIntentSignalSupport.containsAvailabilityRuleCreateSignals(lower)) return VisionIntent.CREATE_AVAILABILITY_RULE;
        if (visionIntentSignalSupport.containsAvailabilityRuleUpdateSignals(lower)) return VisionIntent.UPDATE_AVAILABILITY_RULE;
        if (visionIntentSignalSupport.containsAvailabilityRuleDeleteSignals(lower)) return VisionIntent.DELETE_AVAILABILITY_RULE;
        if (visionIntentSignalSupport.containsAvailabilityExceptionCreateSignals(lower)) return VisionIntent.CREATE_AVAILABILITY_EXCEPTION;
        if (visionIntentSignalSupport.containsAvailabilityExceptionUpdateSignals(lower)) return VisionIntent.UPDATE_AVAILABILITY_EXCEPTION;
        if (visionIntentSignalSupport.containsAvailabilityExceptionDeleteSignals(lower)) return VisionIntent.DELETE_AVAILABILITY_EXCEPTION;
        if (visionIntentSignalSupport.containsBookingConfirmSignals(lower)) {
            return VisionIntent.CONFIRM_BOOKING;
        }
        if (visionIntentSignalSupport.containsBookingCancelSignals(lower)) {
            return VisionIntent.CANCEL_BOOKING;
        }
        if (visionIntentSignalSupport.containsBookingRejectSignals(lower)) return VisionIntent.REJECT_BOOKING;
        if (visionIntentSignalSupport.containsBookingCompleteSignals(lower)) return VisionIntent.COMPLETE_BOOKING;
        if (visionIntentSignalSupport.containsBookingNoShowSignals(lower)) return VisionIntent.MARK_BOOKING_NO_SHOW;
        if (visionIntentSignalSupport.containsOfferingArchiveSignals(lower)) return VisionIntent.ARCHIVE_OFFERING;
        if (visionIntentSignalSupport.containsQuestUpdateSignals(lower)) return VisionIntent.UPDATE_QUEST;
        if (visionIntentSignalSupport.containsOfferingCreateSignals(lower)) return VisionIntent.CREATE_OFFERING;
        if (visionIntentSignalSupport.containsOfferingUpdateSignals(lower)) return VisionIntent.UPDATE_OFFERING;
        if (visionIntentSignalSupport.containsBookingCreateSignals(lower)) return VisionIntent.CREATE_BOOKING;
        if (visionIntentSignalSupport.containsThingArchiveSignals(lower)) return VisionIntent.ARCHIVE_THING;
        if (visionIntentSignalSupport.containsThingUpdateSignals(lower)) return VisionIntent.UPDATE_THING;
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
