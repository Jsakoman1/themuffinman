package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.common.normalization.TextValueNormalizer;

import java.util.regex.Pattern;

final class VisionIntentSignalSupport {

    VisionIntent overrideSnapshotIntent(VisionIntent semanticIntent, String lower) {
        if (semanticIntent == null || semanticIntent == VisionIntent.UNSUPPORTED || lower == null || lower.isBlank()) {
            return null;
        }
        if (semanticIntent == VisionIntent.VIEW_CIRCLES || semanticIntent == VisionIntent.VIEW_CIRCLE_DETAIL) {
            if (containsCircleCreateSignals(lower)) {
                return VisionIntent.CREATE_CIRCLE;
            }
            if (containsCircleRequestAcceptSignals(lower)) {
                return VisionIntent.ACCEPT_CIRCLE_REQUEST;
            }
            if (containsCircleRequestDeleteSignals(lower)) {
                return VisionIntent.DELETE_CIRCLE_REQUEST;
            }
            if (containsCircleRequestCreateSignals(lower)) {
                return VisionIntent.CREATE_CIRCLE_REQUEST;
            }
            if (containsCircleUpdateSignals(lower)) {
                return VisionIntent.UPDATE_CIRCLE;
            }
            if (containsCircleDeleteSignals(lower)) {
                return VisionIntent.DELETE_CIRCLE;
            }
        }
        if (semanticIntent == VisionIntent.VIEW_THINGS && containsThingDetailSignals(lower)) {
            return VisionIntent.VIEW_THING_DETAIL;
        }
        if (semanticIntent == VisionIntent.VIEW_APPLICATIONS || semanticIntent == VisionIntent.VIEW_APPLICATION_DETAIL) {
            if (containsApplicationCreateSignals(lower)) {
                return VisionIntent.CREATE_APPLICATION;
            }
            if (containsApplicationUpdateSignals(lower)) {
                return VisionIntent.UPDATE_APPLICATION;
            }
            if (containsApplicationWithdrawSignals(lower)) {
                return VisionIntent.WITHDRAW_APPLICATION;
            }
            if (containsApplicationApproveSignals(lower)) {
                return VisionIntent.APPROVE_APPLICATION;
            }
            if (containsApplicationDeclineSignals(lower)) {
                return VisionIntent.DECLINE_APPLICATION;
            }
        }
        if (semanticIntent == VisionIntent.VIEW_PROFILE || semanticIntent == VisionIntent.VIEW_SETTINGS) {
            if (containsProfileLocationUpdateSignals(lower)) {
                return VisionIntent.UPDATE_PROFILE_LOCATION;
            }
            if (containsProfileUpdateSignals(lower)) {
                return VisionIntent.UPDATE_PROFILE;
            }
        }
        if (semanticIntent == VisionIntent.VIEW_BUSINESS || semanticIntent == VisionIntent.VIEW_BUSINESS_AVAILABILITY || semanticIntent == VisionIntent.VIEW_BUSINESS_BOOKINGS) {
            if (containsBusinessAvailabilitySignals(lower)) {
                return VisionIntent.VIEW_BUSINESS_AVAILABILITY;
            }
            if (containsBusinessBookingSignals(lower)) {
                return VisionIntent.VIEW_BUSINESS_BOOKINGS;
            }
            if (containsBusinessPageSignals(lower)) {
                return VisionIntent.VIEW_BUSINESS;
            }
        }
        if ((semanticIntent == VisionIntent.VIEW_PROFILE || semanticIntent == VisionIntent.VIEW_SETTINGS)
                && containsBusinessAvailabilitySignals(lower)) {
            return VisionIntent.VIEW_BUSINESS_AVAILABILITY;
        }
        if ((semanticIntent == VisionIntent.VIEW_PROFILE || semanticIntent == VisionIntent.VIEW_SETTINGS)
                && containsBusinessPageSignals(lower)) {
            return VisionIntent.VIEW_BUSINESS;
        }
        if (semanticIntent == VisionIntent.VIEW_CHAT_WORKSPACE && containsChatSignals(lower)) {
            return VisionIntent.OPEN_CHAT;
        }
        if (semanticIntent == VisionIntent.VIEW_QUEST_DETAIL && containsApplicationCreateSignals(lower)) {
            return VisionIntent.CREATE_APPLICATION;
        }
        return null;
    }

    boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            if (containsCandidate(value, candidate)) {
                return true;
            }
        }
        return false;
    }

    boolean containsDiscoverySignals(String value) {
        return containsAny(value,
                "open quests",
                "available quests",
                "show open quests",
                "show quests",
                "find quests",
                "browse quests",
                "search quests",
                "looking for work",
                "looking for jobs",
                "what quests",
                "what can i do",
                "odd jobs",
                "jobs near",
                "work near",
                "help wanted",
                "recommend a quest",
                "recommend work");
    }

    boolean containsSearchSignals(String value) {
        return containsAny(value,
                "search for",
                "find me",
                "find ",
                "look for",
                "browse all",
                "discover anything",
                "show me something",
                "what is available",
                "what's available",
                "who can help",
                "people who",
                "things that",
                "compare results",
                "compare these",
                "compare options",
                "compare",
                "side by side",
                "which is better",
                "something nearby",
                "anything nearby");
    }

    boolean containsChatSignals(String value) {
        return containsAny(value,
                "open chat",
                "start chat",
                "chat with",
                "message",
                "send a message",
                "dm",
                "direct message",
                "talk to");
    }

    boolean containsSendMessageSignals(String value) {
        return containsAny(value,
                "send message",
                "send a message",
                "direct message",
                "dm ");
    }

    boolean containsChatWorkspaceSignals(String value) {
        return containsAny(value,
                "show chat",
                "open chat workspace",
                "chat workspace",
                "my chat",
                "chat inbox",
                "messages");
    }

    boolean containsProfileSignals(String value) {
        return containsAny(value,
                "my profile",
                "show profile",
                "open profile",
                "profile settings",
                "my account",
                "show my account",
                "who am i");
    }

    boolean containsSettingsSignals(String value) {
        return containsAny(value,
                "settings",
                "show settings",
                "open settings",
                "my settings",
                "account settings");
    }

    boolean containsNotificationPreferenceSignals(String value) {
        return containsAny(value,
                "notification preferences", "notification settings", "notification preference",
                "turn off chat notifications", "turn on chat notifications", "disable chat notifications",
                "disable chat in-app notifications", "disable chat in app notifications", "enable chat in-app notifications", "enable chat in app notifications",
                "turn off chat in-app notifications", "turn off chat in app notifications", "turn on chat in-app notifications", "turn on chat in app notifications",
                "mute chat notifications", "unmute chat notifications",
                "turn off email notifications", "turn on email notifications", "disable email notifications",
                "enable email notifications", "mute push notifications", "unmute push notifications");
    }

    boolean containsNotificationsReadSignals(String value) {
        return containsAny(value,
                "mark all notifications as read", "mark notifications as read",
                "mark notification as read", "mark this notification as read",
                "clear notifications", "clear notification inbox", "read all notifications");
    }

    boolean containsWorkerReleaseSignals(String value) {
        return containsAny(value, "release worker", "remove worker", "free worker slot", "release assigned worker");
    }

    boolean containsWorkerReplacementSignals(String value) {
        return containsAny(value, "replace worker", "reassign worker", "swap worker", "change assigned worker");
    }

    boolean containsBusinessPageSignals(String value) {
        return containsAny(value,
                "my business",
                "show my business",
                "open my business",
                "show business",
                "open business",
                "view business",
                "business page",
                "find a business",
                "find businesses",
                "discover businesses",
                "business profile",
                "business details",
                "business info",
                "business offerings",
                "business services",
                "business gallery",
                "business contacts",
                "business address");
    }

    boolean containsBusinessAvailabilitySignals(String value) {
        return containsAny(value,
                "show business availability",
                "open business availability",
                "view business availability",
                "business availability",
                "business schedule",
                "business calendar",
                "business hours",
                "opening hours",
                "owner dashboard",
                "owner schedule",
                "owner calendar",
                "booking calendar",
                "booking schedule",
                "my bookings",
                "business bookings",
                "availability rules");
    }

    boolean containsBusinessBookingSignals(String value) {
        return containsAny(value,
                "booking requests",
                "booking request",
                "appointment",
                "appointments",
                "appointment list",
                "appointment requests",
                "booking list",
                "bookings list",
                "booking overview",
                "appointment overview",
                "owner bookings",
                "customer bookings");
    }

    boolean containsUserProfileDetailSignals(String value) {
        return containsAny(value,
                "show user",
                "open user",
                "show profile for",
                "open profile for",
                "show profile of",
                "open profile of");
    }

    boolean containsProfileUpdateSignals(String value) {
        return containsAny(value,
                "update my profile",
                "edit my profile",
                "change my profile",
                "change my username",
                "update my username",
                "set my username",
                "change my bio",
                "update my bio",
                "change my profile description",
                "update my profile description",
                "set my profile description");
    }

    boolean containsCirclesSignals(String value) {
        return containsAny(value,
                "my circles",
                "show circles",
                "open circles",
                "go to circles",
                "navigate to circles",
                "circle list",
                "my network");
    }

    boolean containsQuestDetailSignals(String value) {
        return containsAny(value,
                "show quest ",
                "open quest ",
                "quest details",
                "quest detail");
    }

    boolean containsQuestCancelSignals(String value) {
        return containsAny(value, "cancel quest", "cancel job", "cancel work", "cancel my quest", "cancel my job");
    }

    boolean containsQuestReopenSignals(String value) {
        return containsAny(value, "reopen quest", "reopen job", "reopen work", "reopen my quest", "reopen my job", "reopen cancelled quest", "reopen canceled quest");
    }

    boolean containsQuestPauseSignals(String value) {
        return containsAny(value, "pause quest", "pause job", "pause work", "pause my quest", "resume quest", "resume job", "resume work", "unpause quest");
    }

    boolean containsBookingRescheduleSignals(String value) {
        return containsAny(value, "reschedule booking", "reschedule appointment", "move booking", "move appointment", "change booking time");
    }

    boolean containsQuestNewsSignals(String value) {
        return containsAny(value,
                "my news",
                "show news",
                "view quest news",
                "show my quest news",
                "open quest news",
                "view my news",
                "quest news",
                "quest updates",
                "updates",
                "recent updates",
                "activity feed",
                "quest feed",
                "news feed");
    }

    boolean containsNotificationsSignals(String value) {
        return containsAny(value,
                "notifications",
                "my notifications",
                "notification",
                "view inbox",
                "show inbox",
                "open inbox",
                "notification inbox",
                "open notification center",
                "notification center",
                "notification hub",
                "alerts inbox",
                "alerts",
                "alert center",
                "unread notifications");
    }

    boolean containsCircleDetailSignals(String value) {
        return containsAny(value,
                "show circle ",
                "open circle ",
                "circle details",
                "circle detail");
    }

    boolean containsCircleCreateSignals(String value) {
        return containsAny(value,
                "create circle",
                "new circle",
                "make a circle",
                "start a circle");
    }

    boolean containsCircleRequestCreateSignals(String value) {
        if (containsAny(value,
                "send circle request",
                "send a circle request",
                "invite to my circle",
                "invite to my circles",
                "add to my circle",
                "add to my circles",
                "connect with")) {
            return true;
        }
        return (value.contains("invite") || value.contains("add") || value.contains("connect with"))
                && (value.contains("my circle") || value.contains("my circles"));
    }

    boolean containsCircleRequestAcceptSignals(String value) {
        return containsAny(value,
                "accept circle request",
                "accept connection request",
                "accept invite",
                "accept circle invite");
    }

    boolean containsCircleRequestDeleteSignals(String value) {
        return containsAny(value,
                "decline circle request",
                "reject circle request",
                "decline invite",
                "reject invite",
                "cancel circle request",
                "cancel invite",
                "delete circle request",
                "remove circle request");
    }

    boolean containsCircleUpdateSignals(String value) {
        return containsAny(value,
                "update circle",
                "rename circle",
                "edit circle",
                "change circle name");
    }

    boolean containsCircleDeleteSignals(String value) {
        return containsAny(value,
                "delete circle",
                "remove circle");
    }

    boolean containsCircleLeaveSignals(String value) { return containsAny(value, "leave circle", "exit circle", "leave my circle"); }

    boolean containsApplicationCreateSignals(String value) {
        return containsAny(value,
                "apply to quest",
                "apply for quest",
                "apply to job",
                "apply for job",
                "send application",
                "create application",
                "i want to apply",
                "apply to this quest",
                "apply to this job");
    }

    boolean containsApplicationApproveSignals(String value) {
        return containsAny(value,
                "approve application",
                "accept application",
                "approve applicant",
                "accept applicant");
    }

    boolean containsApplicationDeclineSignals(String value) {
        return containsAny(value,
                "decline application",
                "reject application",
                "decline applicant",
                "reject applicant");
    }

    boolean containsApplicationUpdateSignals(String value) {
        return containsAny(value,
                "update my application",
                "edit my application",
                "change my application",
                "update application",
                "edit application",
                "change application",
                "change my offer",
                "update my offer");
    }

    boolean containsApplicationWithdrawSignals(String value) {
        return containsAny(value,
                "withdraw my application",
                "cancel my application",
                "remove my application",
                "withdraw application",
                "cancel application");
    }

    boolean containsApplicationsSignals(String value) {
        return containsAny(value,
                "my applications",
                "show applications",
                "open applications",
                "job applications",
                "quest applications",
                "applications status");
    }

    boolean containsThingsSignals(String value) {
        return containsAny(value,
                "show things",
                "available things",
                "borrow things",
                "my things",
                "my listings",
                "thing listings",
                "available listings",
                "show listings",
                "open listings",
                "borrow listing",
                "share a thing",
                "share things");
    }

    boolean containsThingDetailSignals(String value) {
        return containsAny(value,
                "show thing ", "open thing ", "view thing ",
                "thing details", "thing detail", "listing details", "listing detail");
    }
    boolean containsThingUpdateSignals(String value) { return containsAny(value, "update thing", "edit thing", "update listing", "edit listing"); }
    boolean containsThingArchiveSignals(String value) { return containsAny(value, "archive thing", "archive listing", "remove my listing"); }

    boolean containsBorrowRequestSignals(String value) {
        return containsAny(value, "my borrow requests", "my loans", "borrow requests", "loan requests", "things I borrowed");
    }

    boolean containsChatMessageEditSignals(String value) {
        return containsAny(value, "edit message", "edit my message", "change message");
    }

    boolean containsChatMessageReplySignals(String value) {
        return containsAny(value, "reply to message", "reply message", "respond to message");
    }

    boolean containsChatMessageReactionSignals(String value) {
        return containsAny(value, "react to message", "add reaction", "react message");
    }

    boolean containsBusinessProfileCreateSignals(String value) {
        return containsAny(value, "create business profile", "create business page", "set up business profile");
    }

    boolean containsBusinessProfileUpdateSignals(String value) {
        return containsAny(value, "update business profile", "edit business profile", "change business profile");
    }

    boolean containsGalleryCreateSignals(String value) { return containsAny(value, "add gallery image", "add gallery photo", "upload gallery image"); }
    boolean containsGalleryUpdateSignals(String value) { return containsAny(value, "update gallery image", "edit gallery image", "change gallery image"); }
    boolean containsGalleryDeleteSignals(String value) { return containsAny(value, "delete gallery image", "remove gallery image"); }
    boolean containsAvailabilityRuleCreateSignals(String value) { return containsAny(value, "create availability rule", "add availability rule", "set availability rule"); }
    boolean containsAvailabilityRuleUpdateSignals(String value) { return containsAny(value, "update availability rule", "edit availability rule"); }
    boolean containsAvailabilityRuleDeleteSignals(String value) { return containsAny(value, "delete availability rule", "remove availability rule"); }
    boolean containsAvailabilityExceptionCreateSignals(String value) { return containsAny(value, "create availability exception", "block availability", "add availability exception"); }
    boolean containsAvailabilityExceptionUpdateSignals(String value) { return containsAny(value, "update availability exception", "edit availability exception"); }
    boolean containsAvailabilityExceptionDeleteSignals(String value) { return containsAny(value, "delete availability exception", "remove availability exception", "unblock availability"); }

    boolean containsBookingConfirmSignals(String value) {
        return containsAny(value, "confirm booking", "approve booking", "accept booking");
    }

    boolean containsBookingCancelSignals(String value) {
        return containsAny(value, "cancel booking", "cancel appointment");
    }

    boolean containsBookingRejectSignals(String value) { return containsAny(value, "reject booking", "decline booking"); }
    boolean containsBookingCompleteSignals(String value) { return containsAny(value, "complete booking", "complete appointment"); }
    boolean containsBookingNoShowSignals(String value) { return containsAny(value, "mark booking no show", "mark appointment no show", "customer no show"); }
    boolean containsOfferingArchiveSignals(String value) { return containsAny(value, "archive offering", "archive service", "deactivate offering"); }
    boolean containsQuestUpdateSignals(String value) { return containsAny(value, "update quest", "edit quest", "rename quest", "edit job"); }
    boolean containsOfferingCreateSignals(String value) { return containsAny(value, "create offering", "create service", "add offering"); }
    boolean containsOfferingUpdateSignals(String value) { return containsAny(value, "update offering", "edit offering", "rename offering"); }
    boolean containsBookingCreateSignals(String value) { return containsAny(value, "book offering", "create booking", "make appointment", "book appointment"); }

    boolean containsApplicationDetailSignals(String value) {
        return containsAny(value,
                "show application ",
                "open application ",
                "application details",
                "application detail");
    }

    boolean containsProfileLocationUpdateSignals(String value) {
        return containsAny(value,
                "update my location",
                "change my location",
                "set my location",
                "hide my location",
                "turn off my location",
                "use exact location",
                "use approximate location");
    }

    boolean shouldKeepExistingConversation(String normalizedPrompt, VisionIntent detectedIntent, double semanticIntentConfidence) {
        if (semanticIntentConfidence >= 0.60d) {
            return false;
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return true;
        }
        String lower = TextValueNormalizer.lowerToEmpty(normalizedPrompt);
        if (containsExplicitEntityFamilySignal(lower, detectedIntent)) {
            return false;
        }
        return isLikelyFollowUpPrompt(lower);
    }

    boolean sameIntentWorkspaceFamily(VisionIntent left, VisionIntent right) {
        String leftFamily = workspaceFamily(left);
        return leftFamily != null && leftFamily.equals(workspaceFamily(right));
    }

    private boolean containsExplicitEntityFamilySignal(String value, VisionIntent intent) {
        if (value == null || value.isBlank() || intent == null) {
            return false;
        }
        return switch (intent) {
            case CREATE_QUEST, DISCOVER_QUESTS, VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> containsAny(value, "quest", "quests", "job", "jobs", "work", "news", "updates", "activity feed");
            case VIEW_NOTIFICATIONS -> containsAny(value, "notification", "notifications", "notification center", "alerts", "alert");
            case VIEW_ACTIVITY -> containsAny(value, "activity", "recent activity", "resume", "continue task", "history");
            case CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                 UPDATE_CIRCLE, DELETE_CIRCLE, VIEW_CIRCLES, VIEW_CIRCLE_DETAIL -> containsAny(value, "circle", "circles");
            case CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION,
                 DECLINE_APPLICATION, VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL -> containsAny(value, "application", "applications");
            case UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, VIEW_PROFILE, VIEW_USER_PROFILE -> containsAny(value, "profile", "username", "bio", "location", "settings");
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY -> containsAny(value, "business", "page", "profile", "schedule", "availability", "hours", "dashboard", "bookings");
            case OPEN_CHAT, VIEW_CHAT_WORKSPACE -> containsAny(value, "chat", "message", "dm", "talk");
            case VIEW_CHAT_ATTACHMENT -> containsAny(value, "attachment", "file", "photo", "image") && containsAny(value, "chat", "message");
            default -> false;
        };
    }

    private boolean isLikelyFollowUpPrompt(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.trim();
        int wordCount = normalized.split("\\s+").length;
        if (wordCount <= 2) {
            return true;
        }
        return containsAny(normalized,
                "and ",
                "also ",
                "what about",
                "same ",
                "that ",
                "this ",
                "instead",
                "change it",
                "switch it",
                "continue",
                "keep going");
    }

    private String workspaceFamily(VisionIntent intent) {
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION -> "profile";
            case VIEW_NOTIFICATIONS -> "notifications";
            case VIEW_ACTIVITY -> "activity";
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY -> "business";
            case VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST,
                    DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE -> "circles";
            case VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, CREATE_APPLICATION, UPDATE_APPLICATION,
                    WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "applications";
            case CREATE_QUEST, VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> "quests";
            case VIEW_CHAT_WORKSPACE, OPEN_CHAT, VIEW_CHAT_ATTACHMENT -> "chat";
            default -> null;
        };
    }

    private boolean containsCandidate(String value, String candidate) {
        if (candidate.indexOf(' ') >= 0) {
            return value.contains(candidate);
        }
        return Pattern.compile("\\b" + Pattern.quote(candidate) + "\\b").matcher(value).find();
    }
}
