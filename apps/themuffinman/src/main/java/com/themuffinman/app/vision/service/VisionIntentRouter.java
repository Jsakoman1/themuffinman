package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class VisionIntentRouter {

    private final VisionProperties visionProperties;

    public VisionIntentRouter(VisionProperties visionProperties) {
        this.visionProperties = visionProperties;
    }

    public VisionIntent detectIntent(String prompt) {
        return detectIntent(prompt, null);
    }

    public VisionIntent detectIntent(String prompt, VisionPromptUnderstandingResult understanding) {
        VisionIntent semanticIntent = understanding == null
                ? VisionIntent.UNSUPPORTED
                : understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported();
        if (semanticIntent == VisionIntent.DISCOVER_QUESTS) {
            return VisionIntent.DISCOVER_QUESTS;
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
        if (semanticIntent == VisionIntent.VIEW_CIRCLES) {
            return VisionIntent.VIEW_CIRCLES;
        }
        if (semanticIntent == VisionIntent.VIEW_APPLICATION_DETAIL) {
            return VisionIntent.VIEW_APPLICATION_DETAIL;
        }
        if (semanticIntent == VisionIntent.VIEW_APPLICATIONS) {
            return VisionIntent.VIEW_APPLICATIONS;
        }
        String lower = prompt.toLowerCase(Locale.ROOT);
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
        if (containsCircleDeleteSignals(lower)) {
            return VisionIntent.DELETE_CIRCLE;
        }
        if (containsCircleUpdateSignals(lower)) {
            return VisionIntent.UPDATE_CIRCLE;
        }
        if (containsApplicationWithdrawSignals(lower)) {
            return VisionIntent.WITHDRAW_APPLICATION;
        }
        if (containsApplicationUpdateSignals(lower)) {
            return VisionIntent.UPDATE_APPLICATION;
        }
        if (containsApplicationApproveSignals(lower)) {
            return VisionIntent.APPROVE_APPLICATION;
        }
        if (containsApplicationDeclineSignals(lower)) {
            return VisionIntent.DECLINE_APPLICATION;
        }
        if (containsProfileLocationUpdateSignals(lower)) {
            return VisionIntent.UPDATE_PROFILE_LOCATION;
        }
        if (containsApplicationCreateSignals(lower)) {
            return VisionIntent.CREATE_APPLICATION;
        }
        if (containsProfileUpdateSignals(lower)) {
            return VisionIntent.UPDATE_PROFILE;
        }
        if (containsSettingsSignals(lower)) {
            return VisionIntent.VIEW_SETTINGS;
        }
        if (containsUserProfileDetailSignals(lower)) {
            return VisionIntent.VIEW_USER_PROFILE;
        }
        if (containsCircleDetailSignals(lower)) {
            return VisionIntent.VIEW_CIRCLE_DETAIL;
        }
        if (containsQuestDetailSignals(lower)) {
            return VisionIntent.VIEW_QUEST_DETAIL;
        }
        if (containsProfileSignals(lower)) {
            return VisionIntent.VIEW_PROFILE;
        }
        if (containsCirclesSignals(lower)) {
            return VisionIntent.VIEW_CIRCLES;
        }
        if (containsApplicationDetailSignals(lower)) {
            return VisionIntent.VIEW_APPLICATION_DETAIL;
        }
        if (containsApplicationsSignals(lower)) {
            return VisionIntent.VIEW_APPLICATIONS;
        }
        if (containsDiscoverySignals(lower)) {
            return VisionIntent.DISCOVER_QUESTS;
        }
        if (containsChatWorkspaceSignals(lower)) {
            return VisionIntent.VIEW_CHAT_WORKSPACE;
        }
        if (containsChatSignals(lower)) {
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
        if (containsAny(lower,
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

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsDiscoverySignals(String value) {
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

    private boolean containsChatSignals(String value) {
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

    private boolean containsChatWorkspaceSignals(String value) {
        return containsAny(value,
                "show chat",
                "open chat workspace",
                "chat workspace",
                "my chat",
                "chat inbox",
                "messages");
    }

    private boolean containsProfileSignals(String value) {
        return containsAny(value,
                "my profile",
                "show profile",
                "open profile",
                "profile settings",
                "my account",
                "show my account",
                "who am i");
    }

    private boolean containsSettingsSignals(String value) {
        return containsAny(value,
                "settings",
                "show settings",
                "open settings",
                "my settings",
                "account settings");
    }

    private boolean containsUserProfileDetailSignals(String value) {
        return containsAny(value,
                "show user",
                "open user",
                "show profile for",
                "open profile for",
                "show profile of",
                "open profile of");
    }

    private boolean containsProfileUpdateSignals(String value) {
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

    private boolean containsCirclesSignals(String value) {
        return containsAny(value,
                "my circles",
                "show circles",
                "open circles",
                "circle list",
                "my network");
    }

    private boolean containsQuestDetailSignals(String value) {
        return containsAny(value,
                "show quest ",
                "open quest ",
                "quest details",
                "quest detail");
    }

    private boolean containsCircleDetailSignals(String value) {
        return containsAny(value,
                "show circle ",
                "open circle ",
                "circle details",
                "circle detail");
    }

    private boolean containsCircleCreateSignals(String value) {
        return containsAny(value,
                "create circle",
                "new circle",
                "make a circle",
                "start a circle");
    }

    private boolean containsCircleRequestCreateSignals(String value) {
        return containsAny(value,
                "send circle request",
                "send a circle request",
                "invite to my circle",
                "invite to my circles",
                "add to my circle",
                "add to my circles",
                "connect with");
    }

    private boolean containsCircleRequestAcceptSignals(String value) {
        return containsAny(value,
                "accept circle request",
                "accept connection request",
                "accept invite",
                "accept circle invite");
    }

    private boolean containsCircleRequestDeleteSignals(String value) {
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

    private boolean containsCircleUpdateSignals(String value) {
        return containsAny(value,
                "update circle",
                "rename circle",
                "edit circle",
                "change circle name");
    }

    private boolean containsCircleDeleteSignals(String value) {
        return containsAny(value,
                "delete circle",
                "remove circle");
    }

    private boolean containsApplicationCreateSignals(String value) {
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

    private boolean containsApplicationApproveSignals(String value) {
        return containsAny(value,
                "approve application",
                "accept application",
                "approve applicant",
                "accept applicant");
    }

    private boolean containsApplicationDeclineSignals(String value) {
        return containsAny(value,
                "decline application",
                "reject application",
                "decline applicant",
                "reject applicant");
    }

    private boolean containsApplicationUpdateSignals(String value) {
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

    private boolean containsApplicationWithdrawSignals(String value) {
        return containsAny(value,
                "withdraw my application",
                "cancel my application",
                "remove my application",
                "withdraw application",
                "cancel application");
    }

    private boolean containsApplicationsSignals(String value) {
        return containsAny(value,
                "my applications",
                "show applications",
                "open applications",
                "job applications",
                "quest applications",
                "applications status");
    }

    private boolean containsApplicationDetailSignals(String value) {
        return containsAny(value,
                "show application ",
                "open application ",
                "application details",
                "application detail");
    }

    private boolean containsProfileLocationUpdateSignals(String value) {
        return containsAny(value,
                "update my location",
                "change my location",
                "set my location",
                "hide my location",
                "turn off my location",
                "use exact location",
                "use approximate location");
    }
}
