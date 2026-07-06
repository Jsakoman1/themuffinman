package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
                .examples(List.of(
                        example("create a paid quest called Move my sofa for tomorrow at 7 pm in Zurich for 20 euros", Map.of(
                                "quest_title", "Move my sofa",
                                "quest_description", "Move my sofa for tomorrow at 7 pm in Zurich",
                                "reward_amount", "20",
                                "schedule_mode", "fixed",
                                "visibility", "PUBLIC",
                                "location_mode", "custom"
                        )),
                        example("create a free quest to help carry boxes on saturday evening", Map.of(
                                "quest_title", "help carry boxes",
                                "reward_amount", "0",
                                "schedule_mode", "agreement",
                                "visibility", "PUBLIC",
                                "location_mode", "off"
                        ))
                ))
                .slots(List.of(
                        slot("quest_title", "questTitle", "short_text", true, "Short user-facing quest title.", List.of(), List.of("title", "name", "quest"), List.of("create quest", "quest title")),
                        slot("quest_description", "questDescription", "long_text", true, "Plain description of what the user needs.", List.of(), List.of("description", "details", "what needs to happen", "need"), List.of("quest title", "reward", "visibility")),
                        slot("reward_amount", "reward.amount", "money_or_free", true, "Reward amount, or 0 when the user explicitly says the quest is free.", List.of(), List.of("reward", "price", "amount", "payment", "free"), List.of("quest title", "description")),
                        slot("schedule_mode", "schedule.mode", "enum", true, "Timing mode.", List.of("fixed", "agreement"), List.of("schedule", "time", "when"), List.of("location", "reward")),
                        slot("scheduled_date", "schedule.scheduledDate", "date", false, "Local calendar date for fixed timing.", List.of(), List.of("date", "day", "day of month"), List.of("time", "location")),
                        slot("scheduled_time", "schedule.scheduledTime", "time", false, "Local time of day for fixed timing.", List.of(), List.of("time", "hour", "evening", "morning"), List.of("date", "location")),
                        slot("visibility", "visibility", "enum", true, "Quest audience.", List.of("PUBLIC", "CIRCLES"), List.of("public", "circles", "private"), List.of("reward", "schedule")),
                        slot("location_mode", "location.mode", "enum", true, "Location source.", List.of("profile", "custom", "off"), List.of("location", "address", "profile", "off"), List.of("reward", "visibility")),
                        slot("location_label", "location.label", "location_text", false, "Custom place or address when location mode is custom.", List.of(), List.of("place", "address", "city", "location"), List.of("off", "profile")),
                        slot("location_candidate_confirmation", "location.candidateConfirmation", "enum", false, "Location candidate decision.", List.of("resolved", "typed"), List.of("resolved", "typed"), List.of("reward", "schedule"))
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
                .examples(List.of(
                        example("create new circle Lover", Map.of("circle_name", "Lover")),
                        example("circle called Core Team", Map.of("circle_name", "Core Team"))
                ))
                .slots(List.of(
                        slot("circle_name", "name", "short_text", true, "Short user-facing circle name.", List.of(), List.of("name", "title", "circle"), List.of("create circle", "new circle", "rename circle"))
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
                .examples(List.of(
                        example("send circle request to josip", Map.of("target_user", "Josip")),
                        example("invite to my circle ana", Map.of("target_user", "Ana"))
                ))
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Username, email, or name fragment for the person who should receive the circle request.", List.of(), List.of("user", "member", "person", "contact"), List.of("circle", "quest", "application"))
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
                .examples(List.of(
                        example("accept circle request from josip", Map.of("target_user", "Josip")),
                        example("accept invite from ana", Map.of("target_user", "Ana"))
                ))
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Username, email, or name fragment that identifies one incoming circle request.", List.of(), List.of("user", "member", "person", "contact"), List.of("circle", "quest", "application"))
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
                .examples(List.of(
                        example("decline circle request from josip", Map.of("target_user", "Josip")),
                        example("reject invite from ana", Map.of("target_user", "Ana"))
                ))
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Username, email, or name fragment that identifies one pending circle request.", List.of(), List.of("user", "member", "person", "contact"), List.of("circle", "quest", "application"))
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
                .examples(List.of(
                        example("rename circle Friends to Core Team", Map.of(
                                "target_circle_query", "Friends",
                                "circle_name", "Core Team"
                        )),
                        example("update circle Family", Map.of("target_circle_query", "Family"))
                ))
                .slots(List.of(
                        slot("target_circle_query", "circleTarget.query", "circle_reference", true, "Circle id or circle name that identifies one owned circle.", List.of(), List.of("circle", "circle name", "name"), List.of("rename circle", "delete circle", "update circle")),
                        slot("circle_name", "name", "short_text", true, "Replacement circle name.", List.of(), List.of("name", "title"), List.of("rename circle", "old circle name"))
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
                .examples(List.of(
                        example("delete circle Friends", Map.of("target_circle_query", "Friends")),
                        example("remove circle Core Team", Map.of("target_circle_query", "Core Team"))
                ))
                .slots(List.of(
                        slot("target_circle_query", "circleTarget.query", "circle_reference", true, "Circle id or circle name that identifies one owned circle.", List.of(), List.of("circle", "circle name", "name"), List.of("delete circle", "remove circle"))
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
                .examples(List.of(
                        example("apply to quest #42 i can help tomorrow for 20 euros", Map.of(
                                "target_quest_query", "#42",
                                "application_message", "i can help tomorrow",
                                "application_proposed_price", "20"
                        )),
                        example("apply for quest move sofa", Map.of(
                                "target_quest_query", "move sofa"
                        ))
                ))
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or short quest title/query that identifies one applyable quest.", List.of(), List.of("quest", "job", "work"), List.of("application", "circle", "profile")),
                        slot("application_message", "message", "long_text", true, "Application message sent to the quest creator.", List.of(), List.of("message", "note", "cover note"), List.of("price", "reward")),
                        slot("application_proposed_price", "proposedPrice", "money", false, "Proposed price for paid quests only.", List.of(), List.of("price", "amount", "offer", "budget"), List.of("message", "quest title"))
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
                .examples(List.of(
                        example("update my application for quest #42 change message to i can come earlier", Map.of(
                                "target_quest_query", "#42",
                                "application_message", "i can come earlier"
                        )),
                        example("update my application for sofa job price 25", Map.of(
                                "target_quest_query", "sofa job",
                                "application_proposed_price", "25"
                        ))
                ))
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or short quest title/query that identifies one editable pending application.", List.of(), List.of("quest", "job", "work"), List.of("application", "circle", "profile")),
                        slot("application_message", "message", "long_text", false, "Replacement application message when the user wants to change it.", List.of(), List.of("message", "note", "cover note"), List.of("price", "reward")),
                        slot("application_proposed_price", "proposedPrice", "money", false, "Replacement proposed price for paid quests when the user wants to change it.", List.of(), List.of("price", "amount", "offer", "budget"), List.of("message", "quest title"))
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
                .examples(List.of(
                        example("withdraw my application for quest #42", Map.of("target_quest_query", "#42")),
                        example("withdraw application move sofa", Map.of("target_quest_query", "move sofa"))
                ))
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or short quest title/query that identifies one withdrawable pending application.", List.of(), List.of("quest", "job", "work"), List.of("application", "circle", "profile"))
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
                .examples(List.of(
                        example("approve application josip for quest #42", Map.of(
                                "target_quest_query", "#42",
                                "target_user", "Josip"
                        ))
                ))
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or quest wording that identifies one manageable quest.", List.of(), List.of("quest", "job", "work"), List.of("application", "circle", "profile")),
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Applicant username or name fragment that identifies one pending application.", List.of(), List.of("user", "applicant", "person", "member"), List.of("quest", "circle", "profile"))
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
                .examples(List.of(
                        example("decline application josip for quest #42", Map.of(
                                "target_quest_query", "#42",
                                "target_user", "Josip"
                        ))
                ))
                .slots(List.of(
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or quest wording that identifies one manageable quest.", List.of(), List.of("quest", "job", "work"), List.of("application", "circle", "profile")),
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Applicant username or name fragment that identifies one pending application.", List.of(), List.of("user", "applicant", "person", "member"), List.of("quest", "circle", "profile"))
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
                .examples(List.of(
                        example("update my username to jsak and bio to reliable mover", Map.of(
                                "profile_username", "jsak",
                                "profile_description", "reliable mover"
                        )),
                        example("change my profile description to i help move furniture", Map.of(
                                "profile_description", "i help move furniture"
                        ))
                ))
                .slots(List.of(
                        slot("profile_username", "username", "short_text", false, "Updated profile username.", List.of(), List.of("username", "handle", "name"), List.of("bio", "description", "location")),
                        slot("profile_description", "profileDescription", "long_text", false, "Updated profile description or bio.", List.of(), List.of("bio", "description", "about"), List.of("username", "location"))
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
                .examples(List.of(
                        example("set my profile location to zurich switzerland", Map.of(
                                "profile_location_mode", "EXACT",
                                "profile_location_label", "Zurich, Switzerland"
                        )),
                        example("hide my location", Map.of(
                                "profile_location_mode", "OFF"
                        ))
                ))
                .slots(List.of(
                        slot("profile_location_mode", "locationSettings.mode", "enum", true, "Profile location mode.", List.of("OFF", "APPROXIMATE", "EXACT"), List.of("off", "approximate", "exact", "location"), List.of("username", "bio")),
                        slot("profile_location_label", "locationSettings.label", "location_text", false, "Location label or address when the location is not off.", List.of(), List.of("location", "address", "city", "place"), List.of("off", "exact", "approximate"))
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
                .examples(List.of(
                        example("find people who can help move sofa", Map.of("search_query", "move sofa")),
                        example("search for circle friends", Map.of("search_query", "circle friends"))
                ))
                .slots(List.of(
                        slot("search_query", "semanticPlan.searchQuery", "short_text", false, "Broad topic the user wants to explore.", List.of(), List.of("topic", "query", "keyword", "search"), List.of("title", "name"))
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
                .examples(List.of(
                        example("show me open quests for moving help", Map.of("search_query", "moving help")),
                        example("find quests for garden work", Map.of("search_query", "garden work"))
                ))
                .slots(List.of(
                        slot("search_query", "semanticPlan.searchQuery", "short_text", false, "Concrete topic the user wants to browse.", List.of(), List.of("topic", "query", "keyword", "search"), List.of("title", "name"))
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
                .examples(List.of(
                        example("open chat with josip", Map.of("target_user", "Josip")),
                        example("dm Ana", Map.of("target_user", "Ana"))
                ))
                .slots(List.of(
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "Username, email, or name fragment for the intended chat contact.", List.of(), List.of("user", "contact", "member", "person"), List.of("quest", "circle", "application"))
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
                .examples(List.of(
                        example("show chat", Map.of()),
                        example("show my chat workspace", Map.of())
                ))
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
                .examples(List.of(
                        example("show my profile", Map.of()),
                        example("open profile", Map.of())
                ))
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
                .examples(List.of(
                        example("show settings", Map.of()),
                        example("open settings", Map.of())
                ))
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
                        slot("target_user", "semanticPlan.targetUserQuery", "user_reference", true, "User id, username, or email that identifies one profile.", List.of(), List.of("user", "contact", "member", "person"), List.of("circle", "quest", "application"))
                ))
                .examples(List.of(
                        example("show user Josip", Map.of("target_user", "Josip")),
                        example("open profile for Ana", Map.of("target_user", "Ana"))
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
                .examples(List.of(
                        example("show circles", Map.of()),
                        example("show my circles", Map.of())
                ))
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
                        slot("target_circle_query", "circleTarget.query", "circle_reference", true, "Circle id or circle name that identifies one circle.", List.of(), List.of("circle", "circle name", "name"), List.of("quest", "application", "profile"))
                ))
                .examples(List.of(
                        example("open circle Family", Map.of("target_circle_query", "Family")),
                        example("show circle Friends", Map.of("target_circle_query", "Friends"))
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
                        slot("target_quest_query", "questTarget.query", "quest_reference", true, "Quest id or quest title that identifies one visible quest.", List.of(), List.of("quest", "job", "work"), List.of("circle", "application", "profile"))
                ))
                .examples(List.of(
                        example("show quest #42", Map.of("target_quest_query", "#42")),
                        example("open quest move sofa", Map.of("target_quest_query", "move sofa"))
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
                .examples(List.of(
                        example("show notifications", Map.of()),
                        example("open notifications", Map.of())
                ))
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
                .examples(List.of(
                        example("show my news", Map.of()),
                        example("open quest news", Map.of())
                ))
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
                .examples(List.of(
                        example("show applications", Map.of()),
                        example("view my applications", Map.of())
                ))
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
                        slot("target_application_query", "applicationTarget.query", "application_reference", true, "Application id or exact quest title that identifies one application.", List.of(), List.of("application", "app", "application id"), List.of("circle", "quest", "profile"))
                ))
                .examples(List.of(
                        example("show application #42", Map.of("target_application_query", "#42")),
                        example("open application for move sofa", Map.of("target_application_query", "move sofa"))
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
        return slot(slotId, fieldName, kind, required, description, allowedValues, List.of(), List.of());
    }

    private VisionSemanticSlotDescriptor slot(
            String slotId,
            String fieldName,
            String kind,
            boolean required,
            String description,
            List<String> allowedValues,
            List<String> aliases,
            List<String> antiExamples
    ) {
        return VisionSemanticSlotDescriptor.builder()
                .slotId(slotId)
                .fieldName(fieldName)
                .kind(kind)
                .required(required)
                .description(description)
                .allowedValues(allowedValues)
                .aliases(aliases)
                .antiExamples(antiExamples)
                .build();
    }

    private VisionSemanticRouteExampleDescriptor example(String input, Map<String, String> expectedSlots) {
        return VisionSemanticRouteExampleDescriptor.builder()
                .input(input)
                .expectedSlots(expectedSlots == null ? Map.of() : expectedSlots)
                .build();
    }
}
