package com.themuffinman.app.prompt;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PromptSemanticsSupport {

    public PromptSemanticPlan inferPlan(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return PromptSemanticPlan.empty();
        }

        String normalizedPrompt = normalizePrompt(prompt);
        String lower = normalizedPrompt.toLowerCase(Locale.ROOT);
        if (containsApplicationApproveSignals(lower)) {
            return PromptSemanticPlan.approveApplication(0.8d, "Local prompt signals match approve_application.", inferApprovalTarget(normalizedPrompt));
        }
        if (containsApplicationDeclineSignals(lower)) {
            return PromptSemanticPlan.declineApplication(0.8d, "Local prompt signals match decline_application.", inferApprovalTarget(normalizedPrompt));
        }
        if (containsCircleRequestAcceptSignals(lower)) {
            return PromptSemanticPlan.acceptCircleRequest(0.8d, "Local prompt signals match accept_circle_request.", inferCircleRequestTarget(normalizedPrompt));
        }
        if (containsCircleRequestDeleteSignals(lower)) {
            return PromptSemanticPlan.deleteCircleRequest(0.8d, "Local prompt signals match delete_circle_request.", inferCircleRequestTarget(normalizedPrompt));
        }
        if (containsCircleCreateSignals(lower)) {
            return PromptSemanticPlan.createCircle(0.8d, "Local prompt signals match create_circle.");
        }
        if (containsCircleRequestCreateSignals(lower)) {
            return PromptSemanticPlan.createCircleRequest(0.8d, "Local prompt signals match create_circle_request.", inferCircleRequestTarget(normalizedPrompt));
        }
        if (containsCircleDeleteSignals(lower)) {
            return PromptSemanticPlan.deleteCircle(0.8d, "Local prompt signals match delete_circle.");
        }
        if (containsCircleUpdateSignals(lower)) {
            return PromptSemanticPlan.updateCircle(0.8d, "Local prompt signals match update_circle.");
        }
        if (containsApplicationWithdrawSignals(lower)) {
            return PromptSemanticPlan.withdrawApplication(0.8d, "Local prompt signals match withdraw_application.");
        }
        if (containsApplicationUpdateSignals(lower)) {
            return PromptSemanticPlan.updateApplication(0.8d, "Local prompt signals match update_application.");
        }
        if (containsProfileLocationUpdateSignals(lower)) {
            return PromptSemanticPlan.updateProfileLocation(0.8d, "Local prompt signals match update_profile_location.");
        }
        if (containsApplicationCreateSignals(lower)) {
            return PromptSemanticPlan.createApplication(0.8d, "Local prompt signals match create_application.");
        }
        if (containsProfileUpdateSignals(lower)) {
            return PromptSemanticPlan.updateProfile(0.8d, "Local prompt signals match update_profile.");
        }
        if (containsSettingsSignals(lower)) {
            return PromptSemanticPlan.viewSettings(0.8d, "Local prompt signals match view_settings.");
        }
        if (containsUserProfileDetailSignals(lower)) {
            return PromptSemanticPlan.viewUserProfile(0.8d, "Local prompt signals match view_user_profile.", inferProfileTarget(normalizedPrompt));
        }
        if (containsCircleDetailSignals(lower)) {
            return PromptSemanticPlan.viewCircleDetail(0.8d, "Local prompt signals match view_circle_detail.");
        }
        if (containsQuestDetailSignals(lower)) {
            return PromptSemanticPlan.viewQuestDetail(0.8d, "Local prompt signals match view_quest_detail.");
        }
        if (containsQuestNewsSignals(lower)) {
            return PromptSemanticPlan.viewQuestNews(0.8d, "Local prompt signals match view_quest_news.");
        }
        if (containsProfileSignals(lower)) {
            return PromptSemanticPlan.viewProfile(0.8d, "Local prompt signals match view_profile.");
        }
        if (containsCirclesSignals(lower)) {
            return PromptSemanticPlan.viewCircles(0.8d, "Local prompt signals match view_circles.");
        }
        if (containsApplicationDetailSignals(lower)) {
            return PromptSemanticPlan.viewApplicationDetail(0.8d, "Local prompt signals match view_application_detail.");
        }
        if (containsApplicationsSignals(lower)) {
            return PromptSemanticPlan.viewApplications(0.8d, "Local prompt signals match view_applications.");
        }
        if (containsDiscoverySignals(lower)) {
            return PromptSemanticPlan.discoverQuests(0.8d, "Local prompt signals match discover_quests.", inferDiscoveryQuery(normalizedPrompt));
        }
        if (containsChatWorkspaceSignals(lower)) {
            return PromptSemanticPlan.viewChatWorkspace(0.8d, "Local prompt signals match view_chat_workspace.");
        }
        if (containsChatSignals(lower)) {
            return PromptSemanticPlan.openChat(0.8d, "Local prompt signals match open_chat.", inferChatTarget(normalizedPrompt));
        }
        if (containsCreateQuestSignals(lower)) {
            return PromptSemanticPlan.createQuest(0.8d, "Local prompt signals match create_quest.");
        }
        return PromptSemanticPlan.empty();
    }

    public String normalizePrompt(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.trim().replaceAll("\\s+", " ");
    }

    private String inferDiscoveryQuery(String prompt) {
        if (prompt == null) {
            return "";
        }

        String extracted = prompt.toLowerCase(Locale.ROOT)
                .replace("show me open quests for", " ")
                .replace("show open quests for", " ")
                .replace("show me quests for", " ")
                .replace("show quests for", " ")
                .replace("find quests for", " ")
                .replace("browse quests for", " ")
                .replace("search quests for", " ")
                .replace("looking for work", " ")
                .replace("looking for jobs", " ")
                .replace("jobs near", " ")
                .replace("work near", " ")
                .replace("help wanted", " ")
                .replaceAll("(?i)\\b(open|available|discover|recommend|quest|quests|job|jobs|work|task|tasks|near|around|me|please|for|the|a|an|to)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return extracted;
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
                "edit profile",
                "change profile",
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

    private boolean containsCircleDetailSignals(String value) {
        return containsAny(value,
                "show circle ",
                "open circle ",
                "circle details",
                "circle detail");
    }

    private boolean containsQuestDetailSignals(String value) {
        return containsAny(value,
                "show quest ",
                "open quest ",
                "quest details",
                "quest detail");
    }

    private boolean containsQuestNewsSignals(String value) {
        return containsAny(value,
                "my news",
                "show news",
                "view quest news",
                "show my quest news",
                "open quest news",
                "view my news",
                "quest news",
                "news feed",
                "notifications",
                "updates",
                "recent updates",
                "activity feed");
    }

    private boolean containsCircleCreateSignals(String value) {
        return containsAny(value,
                "create circle",
                "new circle",
                "make a circle",
                "start a circle",
                "create group",
                "new group",
                "make a group",
                "start a group");
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
                "submit application",
                "send my application",
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

    private boolean containsChatSignals(String value) {
        return containsAny(value,
                "open chat",
                "start chat",
                "chat with",
                "message",
                "send message",
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

    private boolean containsCreateQuestSignals(String value) {
        if (containsAny(value,
                "create quest",
                "create quests",
                "create a quest",
                "create a new quest",
                "create a paid quest",
                "new quest",
                "new quests",
                "post a quest",
                "post quest",
                "post quests",
                "need someone",
                "need somebody",
                "need somebody to help",
                "need somebody to move",
                "looking for someone",
                "i need help",
                "i need somebody",
                "can someone",
                "can somebody",
                "task for someone",
                "help me with",
                "help me move",
                "help moving",
                "move my sofa",
                "move my couch",
                "move furniture",
                "moving sofa",
                "quests")) {
            return true;
        }

        return value.contains("create")
                && value.contains("quest")
                && !value.contains("circle");
    }

    private String inferChatTarget(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.replaceAll("(?i)^.*?(chat with|open chat with|start chat with|message|send a message to|dm|direct message|talk to)\\s+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String inferApprovalTarget(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.replaceAll("(?i)^.*?(approve applicant|accept applicant|approve application|accept application|decline applicant|reject applicant|decline application|reject application)\\s+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String inferCircleRequestTarget(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.replaceAll("(?i)^.*?(send a circle request to|send circle request to|invite to my circle|invite to my circles|add to my circle|add to my circles|connect with|accept circle request from|accept connection request from|accept invite from|accept circle invite from|decline circle request from|reject circle request from|decline invite from|reject invite from|cancel circle request to|cancel invite to|delete circle request with|remove circle request with)\\s+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String inferProfileTarget(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.replaceAll("(?i)^.*?(show user|open user|show profile for|open profile for|show profile of|open profile of)\\s+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
