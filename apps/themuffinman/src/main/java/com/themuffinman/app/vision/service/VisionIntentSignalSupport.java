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
        if (semanticIntent == VisionIntent.VIEW_BUSINESS || semanticIntent == VisionIntent.VIEW_BUSINESS_AVAILABILITY) {
            if (containsBusinessAvailabilitySignals(lower)) {
                return VisionIntent.VIEW_BUSINESS_AVAILABILITY;
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

    boolean containsBusinessPageSignals(String value) {
        return containsAny(value,
                "my business",
                "show my business",
                "open my business",
                "show business",
                "open business",
                "view business",
                "business page",
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
            case CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                 UPDATE_CIRCLE, DELETE_CIRCLE, VIEW_CIRCLES, VIEW_CIRCLE_DETAIL -> containsAny(value, "circle", "circles");
            case CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION,
                 DECLINE_APPLICATION, VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL -> containsAny(value, "application", "applications");
            case UPDATE_PROFILE, UPDATE_PROFILE_LOCATION, VIEW_PROFILE, VIEW_USER_PROFILE -> containsAny(value, "profile", "username", "bio", "location", "settings");
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY -> containsAny(value, "business", "page", "profile", "schedule", "availability", "hours", "dashboard", "bookings");
            case OPEN_CHAT, VIEW_CHAT_WORKSPACE -> containsAny(value, "chat", "message", "dm", "talk");
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
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY -> "business";
            case VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST,
                    DELETE_CIRCLE_REQUEST, UPDATE_CIRCLE, DELETE_CIRCLE -> "circles";
            case VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL, CREATE_APPLICATION, UPDATE_APPLICATION,
                    WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "applications";
            case CREATE_QUEST, VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> "quests";
            case VIEW_CHAT_WORKSPACE, OPEN_CHAT -> "chat";
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
