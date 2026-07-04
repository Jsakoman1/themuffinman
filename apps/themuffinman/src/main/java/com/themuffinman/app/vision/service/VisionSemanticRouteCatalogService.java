package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class VisionSemanticRouteCatalogService {

    public List<VisionSemanticRouteDescriptor> allowedRoutes(AppUser currentUser) {
        if (currentUser == null) {
            return List.of();
        }

        return allRoutes();
    }

    public List<VisionSemanticRouteDescriptor> allRoutes() {
        return List.of(
                createQuestRoute(),
                createCircleRoute(),
                createCircleRequestRoute(),
                acceptCircleRequestRoute(),
                deleteCircleRequestRoute(),
                updateCircleRoute(),
                deleteCircleRoute(),
                createApplicationRoute(),
                updateApplicationRoute(),
                withdrawApplicationRoute(),
                approveApplicationRoute(),
                declineApplicationRoute(),
                updateProfileRoute(),
                updateProfileLocationRoute(),
                searchRoute(),
                discoverQuestsRoute(),
                viewThingsRoute(),
                openChatRoute(),
                viewChatWorkspaceRoute(),
                viewProfileRoute(),
                viewSettingsRoute(),
                viewUserProfileRoute(),
                viewCirclesRoute(),
                viewCircleDetailRoute(),
                viewQuestDetailRoute(),
                viewNotificationsRoute(),
                viewQuestNewsRoute(),
                viewApplicationsRoute(),
                viewApplicationDetailRoute()
        );
    }

    public VisionSemanticRouteDescriptor routeForIntent(String intent) {
        if (intent == null || intent.isBlank()) {
            return null;
        }
        return allRoutes().stream()
                .filter(route -> intent.trim().equalsIgnoreCase(route.getIntent()))
                .findFirst()
                .orElse(null);
    }

    public VisionSemanticRouteDescriptor routeForCapabilityId(String capabilityId) {
        if (capabilityId == null || capabilityId.isBlank()) {
            return null;
        }
        return allRoutes().stream()
                .filter(route -> capabilityId.trim().equalsIgnoreCase(route.getCapabilityId()))
                .findFirst()
                .orElse(null);
    }

    public List<String> requiredSlotIdsForIntent(VisionIntent intent) {
        VisionSemanticRouteDescriptor route = routeForIntent(intent == null ? null : intent.name());
        return requiredSlotIds(route);
    }

    public List<String> requiredSlotIdsForCapabilityId(String capabilityId) {
        VisionSemanticRouteDescriptor route = routeForCapabilityId(capabilityId);
        return requiredSlotIds(route);
    }

    public List<String> requiredSlotIds(VisionSemanticRouteDescriptor route) {
        if (route == null || route.getSlots() == null) {
            return List.of();
        }
        return route.getSlots().stream()
                .filter(slot -> slot != null && slot.isRequired() && slot.getSlotId() != null && !slot.getSlotId().isBlank())
                .map(VisionSemanticSlotDescriptor::getSlotId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public SemanticEntityFamily entityFamilyForIntent(VisionIntent intent) {
        if (intent == null) {
            return SemanticEntityFamily.UNKNOWN;
        }
        return switch (intent) {
            case CREATE_QUEST, DISCOVER_QUESTS, VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> SemanticEntityFamily.QUEST;
            case VIEW_NOTIFICATIONS -> SemanticEntityFamily.NOTIFICATIONS;
            case CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                    UPDATE_CIRCLE, DELETE_CIRCLE, VIEW_CIRCLES, VIEW_CIRCLE_DETAIL -> SemanticEntityFamily.CIRCLE;
            case CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION,
                    DECLINE_APPLICATION, VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL -> SemanticEntityFamily.APPLICATION;
            case VIEW_USER_PROFILE, OPEN_CHAT -> SemanticEntityFamily.USER;
            case VIEW_PROFILE, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION -> SemanticEntityFamily.PROFILE;
            case VIEW_SETTINGS -> SemanticEntityFamily.SETTINGS;
            case VIEW_THINGS -> SemanticEntityFamily.UNKNOWN;
            case SEARCH -> SemanticEntityFamily.UNKNOWN;
            default -> SemanticEntityFamily.UNKNOWN;
        };
    }

    public SemanticEntityFamily targetEntityFamilyForIntent(VisionIntent intent) {
        if (intent == null) {
            return SemanticEntityFamily.UNKNOWN;
        }
        return switch (intent) {
            case CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST, OPEN_CHAT, VIEW_USER_PROFILE -> SemanticEntityFamily.USER;
            case UPDATE_CIRCLE, DELETE_CIRCLE, VIEW_CIRCLE_DETAIL -> SemanticEntityFamily.CIRCLE;
            case CREATE_APPLICATION -> SemanticEntityFamily.QUEST;
            case UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION, VIEW_APPLICATION_DETAIL -> SemanticEntityFamily.APPLICATION;
            case VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> SemanticEntityFamily.QUEST;
            case VIEW_NOTIFICATIONS -> SemanticEntityFamily.UNKNOWN;
            default -> SemanticEntityFamily.UNKNOWN;
        };
    }

    public String dtoTypeForIntent(VisionIntent intent) {
        if (intent == null) {
            return "unknown";
        }
        return switch (intent) {
            case CREATE_QUEST -> "QuestRequestDTO";
            case CREATE_CIRCLE -> "CircleGroupRequestDTO";
            case CREATE_CIRCLE_REQUEST -> "CircleRequestCreateDTO";
            case ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST -> "CircleRequestResponseDTO";
            case UPDATE_CIRCLE, DELETE_CIRCLE -> "CircleGroupResponseDTO";
            case CREATE_APPLICATION -> "QuestApplicationRequestDTO";
            case UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION -> "QuestApplicationResponseDTO";
            case UPDATE_PROFILE -> "AppUserRequestDTO";
            case UPDATE_PROFILE_LOCATION -> "UserLocationSettingsRequestDTO";
            case DISCOVER_QUESTS -> "VisionQuestDiscoveryDTO";
            case SEARCH -> "VisionSearchDiscoveryDTO";
            case OPEN_CHAT -> "ChatConversationSummaryDTO";
            case VIEW_CHAT_WORKSPACE -> "ChatWorkspaceDTO";
            case VIEW_PROFILE -> "AppUserResponseDTO";
            case VIEW_USER_PROFILE -> "UserProfileViewDTO";
            case VIEW_SETTINGS -> "AppUserResponseDTO";
            case VIEW_CIRCLES -> "CircleGroupResponseDTO";
            case VIEW_CIRCLE_DETAIL -> "CircleGroupResponseDTO";
            case VIEW_QUEST_DETAIL -> "QuestDetailResponseDTO";
            case VIEW_NOTIFICATIONS -> "DashboardNotificationsSectionDTO";
            case VIEW_QUEST_NEWS -> "List<QuestNewsItemResponseDTO>";
            case VIEW_APPLICATIONS -> "QuestApplicationResponseDTO";
            case VIEW_APPLICATION_DETAIL -> "QuestApplicationDetailResponseDTO";
            case VIEW_THINGS -> "ThingListingListResponseDTO";
            default -> "unknown";
        };
    }

    public String validatorKeyForIntent(VisionIntent intent) {
        if (intent == null) {
            return "none";
        }
        return intent.name().toLowerCase(Locale.ROOT) + "_validator";
    }

    public String executorKeyForIntent(VisionIntent intent) {
        if (intent == null) {
            return "none";
        }
        return intent.name().toLowerCase(Locale.ROOT) + "_executor";
    }

    public double minimumConfidenceForIntent(VisionIntent intent) {
        if (intent == null) {
            return 0.75d;
        }
        return switch (intent) {
            case CREATE_QUEST, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                    UPDATE_CIRCLE, DELETE_CIRCLE, CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION,
                    APPROVE_APPLICATION, DECLINE_APPLICATION, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION -> 0.85d;
            case VIEW_NOTIFICATIONS, VIEW_QUEST_NEWS -> 0.70d;
            case VIEW_THINGS -> 0.70d;
            default -> 0.75d;
        };
    }

    public boolean requiresTargetEntityResolution(VisionIntent intent) {
        if (intent == null) {
            return false;
        }
        return switch (intent) {
            case CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                    UPDATE_CIRCLE, DELETE_CIRCLE,
                    CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION,
                    APPROVE_APPLICATION, DECLINE_APPLICATION,
                    OPEN_CHAT, VIEW_USER_PROFILE,
                    VIEW_CIRCLE_DETAIL, VIEW_QUEST_DETAIL, VIEW_APPLICATION_DETAIL -> true;
            default -> false;
        };
    }

    public double minimumEntityResolutionConfidenceForIntent(VisionIntent intent) {
        if (intent == null) {
            return 0.75d;
        }
        return switch (intent) {
            case CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                    UPDATE_CIRCLE, DELETE_CIRCLE,
                    CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION,
                    APPROVE_APPLICATION, DECLINE_APPLICATION -> 0.88d;
            case OPEN_CHAT, VIEW_USER_PROFILE, VIEW_CIRCLE_DETAIL, VIEW_QUEST_DETAIL, VIEW_APPLICATION_DETAIL -> 0.75d;
            case SEARCH -> 0.75d;
            case VIEW_NOTIFICATIONS, VIEW_QUEST_NEWS -> 0.70d;
            case VIEW_THINGS -> 0.70d;
            default -> 0.75d;
        };
    }

    private VisionSemanticRouteDescriptor createQuestRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.create_quest")
                .entityType("quest")
                .intent("CREATE_QUEST")
                .capabilityId("create_quest")
                .purpose("Create a new quest for the authenticated user after slot collection, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("quest_title", "questTitle", "short_text", true, "Short user-facing quest title.", List.of()),
                        slot("quest_description", "questDescription", "long_text", true, "Plain description of what the user needs.", List.of()),
                        slot("reward_amount", "reward.amount", "money_or_free", true, "Reward amount, or 0 when the user explicitly says the quest is free.", List.of()),
                        slot("schedule_mode", "schedule.mode", "enum", true, "Timing mode.", List.of("fixed", "agreement")),
                        slot("scheduled_date", "schedule.scheduledDate", "date", false, "Local calendar date for fixed timing.", List.of()),
                        slot("scheduled_time", "schedule.scheduledTime", "time", false, "Local time of day for fixed timing.", List.of()),
                        slot("visibility", "visibility", "enum", true, "Quest audience.", List.of("PUBLIC", "CIRCLES")),
                        slot("location_mode", "location.mode", "enum", true, "Location source.", List.of("profile", "custom", "off")),
                        slot("location_label", "location.label", "location_text", false, "Custom place or address when location mode is custom.", List.of()),
                        slot("location_candidate_confirmation", "location.candidateConfirmation", "enum", false, "Location candidate decision.", List.of("resolved", "typed"))
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor createCircleRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.create_circle")
                .entityType("circle")
                .intent("CREATE_CIRCLE")
                .capabilityId("create_circle")
                .purpose("Create a new circle for the authenticated user after slot collection, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("circle_name", "name", "short_text", true, "Short user-facing circle name.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor createCircleRequestRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.create_circle_request")
                .entityType("circle_request")
                .intent("CREATE_CIRCLE_REQUEST")
                .capabilityId("create_circle_request")
                .purpose("Send one new circle request to another user after exact target resolution, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Username, email, or name fragment for the person who should receive the circle request.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor acceptCircleRequestRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.accept_circle_request")
                .entityType("circle_request")
                .intent("ACCEPT_CIRCLE_REQUEST")
                .capabilityId("accept_circle_request")
                .purpose("Accept one incoming circle request after exact target resolution, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Username, email, or name fragment that identifies one incoming circle request.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor deleteCircleRequestRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.delete_circle_request")
                .entityType("circle_request")
                .intent("DELETE_CIRCLE_REQUEST")
                .capabilityId("delete_circle_request")
                .purpose("Decline or cancel one accessible pending circle request after exact target resolution, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Username, email, or name fragment that identifies one pending circle request.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor updateCircleRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.update_circle")
                .entityType("circle")
                .intent("UPDATE_CIRCLE")
                .capabilityId("update_circle")
                .purpose("Rename one owned circle after exact target resolution, draft review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_circle_query", "circleTarget.query", "circle_reference", true, "Circle id or circle name that identifies one owned circle.", List.of()),
                        slot("circle_name", "name", "short_text", true, "Replacement circle name.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor deleteCircleRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.delete_circle")
                .entityType("circle")
                .intent("DELETE_CIRCLE")
                .capabilityId("delete_circle")
                .purpose("Delete one owned circle after exact target resolution, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_circle_query", "circleTarget.query", "circle_reference", true, "Circle id or circle name that identifies one owned circle.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor createApplicationRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.create_application")
                .entityType("application")
                .intent("CREATE_APPLICATION")
                .capabilityId("create_application")
                .purpose("Create a new quest application for the authenticated user after quest resolution, slot collection, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or short quest title/query that identifies one applyable quest.", List.of()),
                        slot("application_message", "message", "long_text", true, "Application message sent to the quest creator.", List.of()),
                        slot("application_proposed_price", "proposedPrice", "money", false, "Proposed price for paid quests only.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor updateApplicationRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.update_application")
                .entityType("application")
                .intent("UPDATE_APPLICATION")
                .capabilityId("update_application")
                .purpose("Update one pending quest application owned by the authenticated user after target resolution, draft review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or short quest title/query that identifies one editable pending application.", List.of()),
                        slot("application_message", "message", "long_text", false, "Replacement application message when the user wants to change it.", List.of()),
                        slot("application_proposed_price", "proposedPrice", "money", false, "Replacement proposed price for paid quests when the user wants to change it.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor withdrawApplicationRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.withdraw_application")
                .entityType("application")
                .intent("WITHDRAW_APPLICATION")
                .capabilityId("withdraw_application")
                .purpose("Withdraw one pending quest application owned by the authenticated user after target resolution, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or short quest title/query that identifies one withdrawable pending application.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor approveApplicationRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.approve_application")
                .entityType("application")
                .intent("APPROVE_APPLICATION")
                .capabilityId("approve_application")
                .purpose("Approve one pending application on a quest managed by the authenticated user after exact target resolution, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or quest wording that identifies one manageable quest.", List.of()),
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Applicant username or name fragment that identifies one pending application.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor declineApplicationRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.decline_application")
                .entityType("application")
                .intent("DECLINE_APPLICATION")
                .capabilityId("decline_application")
                .purpose("Decline one pending application on a quest managed by the authenticated user after exact target resolution, review, and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or quest wording that identifies one manageable quest.", List.of()),
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Applicant username or name fragment that identifies one pending application.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor updateProfileRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.update_profile")
                .entityType("profile")
                .intent("UPDATE_PROFILE")
                .capabilityId("update_profile")
                .purpose("Update the authenticated user's own profile username and description after review and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("profile_username", "username", "short_text", false, "Updated profile username.", List.of()),
                        slot("profile_description", "profileDescription", "long_text", false, "Updated profile description or bio.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor updateProfileLocationRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.update_profile_location")
                .entityType("profile")
                .intent("UPDATE_PROFILE_LOCATION")
                .capabilityId("update_profile_location")
                .purpose("Update the authenticated user's own profile location mode and label after review and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .slots(List.of(
                        slot("profile_location_mode", "locationSettings.mode", "enum", true, "Profile location mode.", List.of("OFF", "APPROXIMATE", "EXACT")),
                        slot("profile_location_label", "locationSettings.label", "location_text", false, "Location label or address when the location is not off.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor searchRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.search")
                .entityType("search")
                .intent("SEARCH")
                .capabilityId("search")
                .purpose("Read-only broad discovery across quests, circles, users, applications, and things.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of(
                        slot("search_query", "semanticPlan.searchQuery", "short_text", false, "Broad topic the user wants to explore.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor viewThingsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_things")
                .entityType("thing")
                .intent("VIEW_THINGS")
                .capabilityId("view_things")
                .purpose("Read-only catalog of available shared things inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor discoverQuestsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.discover_quests")
                .entityType("quest")
                .intent("DISCOVER_QUESTS")
                .capabilityId("discover_quests")
                .purpose("Read-only quest discovery and recommendation for the authenticated user.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of(
                        slot("search_query", "semanticPlan.searchQuery", "short_text", false, "Concrete topic the user wants to browse.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor openChatRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.open_chat")
                .entityType("chat")
                .intent("OPEN_CHAT")
                .capabilityId("open_chat")
                .purpose("Open an existing permitted chat boundary with a contact identified by the user.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Username, email, or name fragment for the intended chat contact.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor viewChatWorkspaceRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_chat_workspace")
                .entityType("chat")
                .intent("VIEW_CHAT_WORKSPACE")
                .capabilityId("view_chat_workspace")
                .purpose("Read-only summary of the authenticated user's chat workspace inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewProfileRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_profile")
                .entityType("profile")
                .intent("VIEW_PROFILE")
                .capabilityId("view_profile")
                .purpose("Read-only self profile snapshot for the authenticated user inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewSettingsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_settings")
                .entityType("settings")
                .intent("VIEW_SETTINGS")
                .capabilityId("view_settings")
                .purpose("Read-only account and location settings snapshot for the authenticated user inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewUserProfileRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_user_profile")
                .entityType("profile")
                .intent("VIEW_USER_PROFILE")
                .capabilityId("view_user_profile")
                .purpose("Read-only detailed snapshot of one user profile inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "User id, username, or email that identifies one profile.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor viewCirclesRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_circles")
                .entityType("circle")
                .intent("VIEW_CIRCLES")
                .capabilityId("view_circles")
                .purpose("Read-only summary of the authenticated user's circles inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewCircleDetailRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_circle_detail")
                .entityType("circle")
                .intent("VIEW_CIRCLE_DETAIL")
                .capabilityId("view_circle_detail")
                .purpose("Read-only detailed snapshot of one owned circle inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of(
                        slot("target_circle_query", "circleTarget.query", "circle_reference", true, "Circle id or circle name that identifies one circle.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor viewQuestDetailRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_quest_detail")
                .entityType("quest")
                .intent("VIEW_QUEST_DETAIL")
                .capabilityId("view_quest_detail")
                .purpose("Read-only detailed snapshot of one visible quest inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or quest title that identifies one visible quest.", List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor viewNotificationsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_notifications")
                .entityType("notification")
                .intent("VIEW_NOTIFICATIONS")
                .capabilityId("view_notifications")
                .purpose("Read-only notifications inbox for the authenticated user inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewQuestNewsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_quest_news")
                .entityType("news")
                .intent("VIEW_QUEST_NEWS")
                .capabilityId("view_quest_news")
                .purpose("Read-only quest news and updates feed for the authenticated user inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewApplicationsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_applications")
                .entityType("application")
                .intent("VIEW_APPLICATIONS")
                .capabilityId("view_applications")
                .purpose("Read-only summary of the authenticated user's quest applications inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewApplicationDetailRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_application_detail")
                .entityType("application")
                .intent("VIEW_APPLICATION_DETAIL")
                .capabilityId("view_application_detail")
                .purpose("Read-only detailed snapshot of one application inside the Vision terminal flow.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of(
                        slot("target_application_query", "applicationTarget.query", "application_reference", true, "Application id or exact quest title that identifies one application.", List.of())
                ))
                .build();
    }

    private VisionSemanticSlotDescriptor slot(
            String slotId,
            String fieldName,
            String kind,
            boolean required,
            String description,
            List<String> allowedValues
    ) {
        return VisionSemanticSlotDescriptor.builder()
                .slotId(slotId)
                .fieldName(fieldName)
                .kind(kind)
                .required(required)
                .description(description)
                .allowedValues(allowedValues)
                .build();
    }
}
