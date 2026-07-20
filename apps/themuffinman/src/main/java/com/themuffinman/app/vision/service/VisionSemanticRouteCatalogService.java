package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
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
                membershipMutationRoute("vision.leave_circle", "LEAVE_CIRCLE", "leave_circle", "Leave an accessible circle after explicit confirmation.", "leave circle 42"),
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
                viewThingDetailRoute(),
                viewBorrowRequestsRoute(),
                openChatRoute(),
                viewChatWorkspaceRoute(),
                syncChatRoute(),
                viewChatAttachmentRoute(),
                editChatMessageRoute(),
                replyToChatMessageRoute(),
                reactToChatMessageRoute(),
                createBusinessProfileRoute(),
                updateBusinessProfileRoute(),
                galleryMutationRoute("vision.create_gallery_image", "CREATE_GALLERY_IMAGE", "create_gallery_image", "Add a business gallery image after explicit confirmation.", "add gallery image https://example.com/image.jpg"),
                galleryMutationRoute("vision.update_gallery_image", "UPDATE_GALLERY_IMAGE", "update_gallery_image", "Update a business gallery image after explicit confirmation.", "update gallery image 42 https://example.com/image.jpg"),
                galleryMutationRoute("vision.delete_gallery_image", "DELETE_GALLERY_IMAGE", "delete_gallery_image", "Delete a business gallery image after explicit confirmation.", "delete gallery image 42"),
                availabilityMutationRoute("vision.create_availability_rule", "CREATE_AVAILABILITY_RULE", "create_availability_rule", "Create an owner availability rule after explicit confirmation.", "create availability rule day 1 from 09:00 to 17:00 every 60 min"),
                availabilityMutationRoute("vision.update_availability_rule", "UPDATE_AVAILABILITY_RULE", "update_availability_rule", "Update an owner availability rule after explicit confirmation.", "update availability rule 42 day 1 from 09:00 to 17:00 every 60 min"),
                availabilityMutationRoute("vision.delete_availability_rule", "DELETE_AVAILABILITY_RULE", "delete_availability_rule", "Delete an owner availability rule after explicit confirmation.", "delete availability rule 42"),
                availabilityMutationRoute("vision.create_availability_exception", "CREATE_AVAILABILITY_EXCEPTION", "create_availability_exception", "Create an owner availability exception after explicit confirmation.", "create availability exception block 2026-08-01T10:00:00Z until 2026-08-01T12:00:00Z"),
                availabilityMutationRoute("vision.update_availability_exception", "UPDATE_AVAILABILITY_EXCEPTION", "update_availability_exception", "Update an owner availability exception after explicit confirmation.", "update availability exception 42"),
                availabilityMutationRoute("vision.delete_availability_exception", "DELETE_AVAILABILITY_EXCEPTION", "delete_availability_exception", "Delete an owner availability exception after explicit confirmation.", "delete availability exception 42"),
                confirmBookingRoute(),
                cancelBookingRoute(),
                rejectBookingRoute(),
                completeBookingRoute(),
                markBookingNoShowRoute(),
                archiveOfferingRoute(),
                updateQuestRoute(),
                createOfferingRoute(),
                updateOfferingRoute(),
                createBookingRoute(),
                rescheduleBookingRoute(),
                markChatReadRoute(),
                markNotificationsReadRoute(),
                markNotificationReadRoute(),
                updateNotificationPreferencesRoute(),
                workerManagementRoute(false),
                workerManagementRoute(true),
                reopenQuestRoute(),
                cancelQuestRoute(),
                pauseQuestRoute(false),
                pauseQuestRoute(true),
                createThingRoute(),
                requestBorrowRoute(),
                cancelBorrowRoute(),
                decideBorrowRoute(),
                returnBorrowRoute(),
                rideRoute("vision.create_ride", "CREATE_RIDE", "create_ride", "Offer a voluntary ride after explicit review and confirmation.", true),
                rideRoute("vision.view_rides", "VIEW_RIDES", "view_rides", "Show permitted voluntary rides or open one backend-authorized ride detail at /rides/{rideId}.", false),
                rideRoute("vision.join_ride", "JOIN_RIDE", "join_ride", "Join a permitted ride after explicit confirmation.", true),
                rideRoute("vision.update_ride", "UPDATE_RIDE", "update_ride", "Update an owned ride after explicit confirmation.", true),
                rideRoute("vision.leave_ride", "LEAVE_RIDE", "leave_ride", "Leave a joined ride after explicit confirmation.", true),
                rideRoute("vision.cancel_ride", "CANCEL_RIDE", "cancel_ride", "Cancel an owned ride after explicit confirmation.", true),
                rideRoute("vision.start_ride", "START_RIDE", "start_ride", "Start a full ride after explicit confirmation.", true),
                rideRoute("vision.complete_ride", "COMPLETE_RIDE", "complete_ride", "Complete an in-progress ride after explicit confirmation.", true),
                thingMutationRoute("vision.update_thing", "UPDATE_THING", "update_thing", "Update an owned thing listing after explicit confirmation.", "update thing 42 title to Ladder"),
                thingMutationRoute("vision.archive_thing", "ARCHIVE_THING", "archive_thing", "Archive an owned thing listing after explicit confirmation.", "archive thing 42"),
                viewProfileRoute(),
                viewSettingsRoute(),
                viewBusinessRoute(),
                viewBusinessAvailabilityRoute(),
                viewBusinessBookingsRoute(),
                viewUserProfileRoute(),
                viewCirclesRoute(),
                viewCircleDetailRoute(),
                viewAccessibleCircleRoute(),
                viewQuestDetailRoute(),
                viewMyWorkRoute(),
                viewNotificationsRoute(),
                viewActivityRoute(),
                viewQuestNewsRoute(),
                viewApplicationsRoute(),
                viewApplicationDetailRoute()
        );
    }

    public List<String> supportedCandidateIntents() {
        return allRoutes().stream()
                .map(VisionSemanticRouteDescriptor::getIntent)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        intents -> {
                            List<String> values = new ArrayList<>(intents);
                            values.add(VisionIntent.UNSUPPORTED.name());
                            return List.copyOf(values);
                        }
                ));
    }

    public List<String> supportedCapabilityIds() {
        List<String> capabilityIds = allRoutes().stream()
                .map(VisionSemanticRouteDescriptor::getCapabilityId)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        capabilityIds.add("unsupported");
        return List.copyOf(capabilityIds);
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
            case CREATE_QUEST, DISCOVER_QUESTS, VIEW_QUEST_DETAIL, VIEW_MY_WORK, VIEW_QUEST_NEWS, REOPEN_QUEST, CANCEL_QUEST, PAUSE_QUEST, RESUME_QUEST -> SemanticEntityFamily.QUEST;
            case RELEASE_WORKER, REPLACE_WORKER -> SemanticEntityFamily.QUEST;
            case RESCHEDULE_BOOKING -> SemanticEntityFamily.BUSINESS;
            case VIEW_NOTIFICATIONS, UPDATE_NOTIFICATION_PREFERENCES -> SemanticEntityFamily.NOTIFICATIONS;
            case VIEW_ACTIVITY -> SemanticEntityFamily.UNKNOWN;
            case CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                    UPDATE_CIRCLE, DELETE_CIRCLE, LEAVE_CIRCLE, VIEW_CIRCLES, VIEW_CIRCLE_DETAIL, VIEW_ACCESSIBLE_CIRCLE -> SemanticEntityFamily.CIRCLE;
            case CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION,
                    DECLINE_APPLICATION, VIEW_APPLICATIONS, VIEW_APPLICATION_DETAIL -> SemanticEntityFamily.APPLICATION;
            case VIEW_USER_PROFILE, OPEN_CHAT -> SemanticEntityFamily.USER;
            case VIEW_CHAT_WORKSPACE, SYNC_CHAT, VIEW_CHAT_ATTACHMENT, EDIT_CHAT_MESSAGE, REPLY_TO_CHAT_MESSAGE, REACT_TO_CHAT_MESSAGE, MARK_CHAT_READ -> SemanticEntityFamily.CHAT;
            case VIEW_PROFILE, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION -> SemanticEntityFamily.PROFILE;
            case VIEW_SETTINGS -> SemanticEntityFamily.SETTINGS;
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY, VIEW_BUSINESS_BOOKINGS -> SemanticEntityFamily.BUSINESS;
            case VIEW_THINGS, VIEW_THING_DETAIL, VIEW_BORROW_REQUESTS -> SemanticEntityFamily.UNKNOWN;
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
            case UPDATE_CIRCLE, DELETE_CIRCLE, LEAVE_CIRCLE, VIEW_CIRCLE_DETAIL, VIEW_ACCESSIBLE_CIRCLE -> SemanticEntityFamily.CIRCLE;
            case CREATE_APPLICATION -> SemanticEntityFamily.QUEST;
            case UPDATE_APPLICATION, WITHDRAW_APPLICATION, APPROVE_APPLICATION, DECLINE_APPLICATION, VIEW_APPLICATION_DETAIL -> SemanticEntityFamily.APPLICATION;
            case VIEW_QUEST_DETAIL, VIEW_QUEST_NEWS -> SemanticEntityFamily.QUEST;
            case REOPEN_QUEST, CANCEL_QUEST, PAUSE_QUEST, RESUME_QUEST -> SemanticEntityFamily.QUEST;
            case RELEASE_WORKER, REPLACE_WORKER -> SemanticEntityFamily.APPLICATION;
            case RESCHEDULE_BOOKING -> SemanticEntityFamily.BUSINESS;
            case VIEW_THING_DETAIL -> SemanticEntityFamily.UNKNOWN;
            case VIEW_NOTIFICATIONS, VIEW_ACTIVITY -> SemanticEntityFamily.UNKNOWN;
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
            case VIEW_BUSINESS -> "BusinessPublicPageDTO";
            case VIEW_BUSINESS_AVAILABILITY -> "BusinessOwnerDashboardDTO";
            case VIEW_BUSINESS_BOOKINGS -> "BusinessBookingListResponseDTO";
            case VIEW_CIRCLES -> "CircleGroupResponseDTO";
            case VIEW_CIRCLE_DETAIL -> "CircleGroupResponseDTO";
            case VIEW_ACCESSIBLE_CIRCLE -> "CircleGroupResponseDTO";
            case VIEW_QUEST_DETAIL -> "QuestDetailResponseDTO";
            case VIEW_MY_WORK -> "List<QuestResponseDTO>";
            case VIEW_THING_DETAIL -> "ThingListingResponseDTO";
            case VIEW_NOTIFICATIONS -> "DashboardNotificationsSectionDTO";
            case VIEW_QUEST_NEWS -> "List<QuestNewsItemResponseDTO>";
            case VIEW_APPLICATIONS -> "QuestApplicationResponseDTO";
            case VIEW_APPLICATION_DETAIL -> "QuestApplicationDetailResponseDTO";
            case VIEW_THINGS -> "ThingListingListResponseDTO";
            case VIEW_BORROW_REQUESTS -> "ThingBorrowRequestResponseDTO";
            case EDIT_CHAT_MESSAGE, REPLY_TO_CHAT_MESSAGE, REACT_TO_CHAT_MESSAGE -> "ChatMessageDTO";
            case CREATE_BUSINESS_PROFILE, UPDATE_BUSINESS_PROFILE -> "BusinessProfileRequestDTO";
            case CREATE_GALLERY_IMAGE, UPDATE_GALLERY_IMAGE -> "BusinessGalleryImageResponseDTO";
            case DELETE_GALLERY_IMAGE -> "ActionResultDTO";
            case LEAVE_CIRCLE -> "ActionResultDTO";
            case UPDATE_THING -> "ThingListingResponseDTO";
            case ARCHIVE_THING -> "ActionResultDTO";
            case CREATE_AVAILABILITY_RULE, UPDATE_AVAILABILITY_RULE -> "BusinessAvailabilityRuleResponseDTO";
            case DELETE_AVAILABILITY_RULE, DELETE_AVAILABILITY_EXCEPTION -> "ActionResultDTO";
            case CREATE_AVAILABILITY_EXCEPTION, UPDATE_AVAILABILITY_EXCEPTION -> "BusinessAvailabilityExceptionResponseDTO";
            case CONFIRM_BOOKING, CANCEL_BOOKING -> "BusinessBookingResponseDTO";
            case REJECT_BOOKING, COMPLETE_BOOKING, MARK_BOOKING_NO_SHOW -> "BusinessBookingResponseDTO";
            case ARCHIVE_OFFERING -> "BusinessOfferingResponseDTO";
            case UPDATE_QUEST -> "QuestResponseDTO";
            case CREATE_OFFERING, UPDATE_OFFERING -> "BusinessOfferingResponseDTO";
            case CREATE_BOOKING -> "BusinessBookingResponseDTO";
            case RESCHEDULE_BOOKING -> "BusinessBookingResponseDTO";
            case MARK_CHAT_READ -> "ActionResultDTO";
            case MARK_NOTIFICATIONS_READ -> "ActionResultDTO";
            case MARK_NOTIFICATION_READ -> "ActionResultDTO";
            case UPDATE_NOTIFICATION_PREFERENCES -> "NotificationPreferenceResponseDTO";
            case RELEASE_WORKER, REPLACE_WORKER -> "QuestApplicationResponseDTO";
            case REOPEN_QUEST -> "QuestResponseDTO";
            case CANCEL_QUEST, PAUSE_QUEST, RESUME_QUEST -> "QuestResponseDTO";
            case CREATE_THING -> "ThingListingRequestDTO";
            case REQUEST_BORROW -> "ThingBorrowRequestDTO";
            case CANCEL_BORROW, DECIDE_BORROW, RETURN_BORROW -> "ThingBorrowRequestResponseDTO";
            case SYNC_CHAT -> "ChatConversationSyncDTO";
            case VIEW_CHAT_ATTACHMENT -> "ChatMessageDTO";
            default -> "unknown";
        };
    }

    public String validatorKeyForIntent(VisionIntent intent) {
        if (intent == null) {
            return "none";
        }
        return TextValueNormalizer.lowerToEmpty(intent.name()) + "_validator";
    }

    public String executorKeyForIntent(VisionIntent intent) {
        if (intent == null) {
            return "none";
        }
        return TextValueNormalizer.lowerToEmpty(intent.name()) + "_executor";
    }

    public double minimumConfidenceForIntent(VisionIntent intent) {
        if (intent == null) {
            return 0.75d;
        }
        return switch (intent) {
            case CREATE_QUEST, CREATE_CIRCLE, CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST,
                    UPDATE_CIRCLE, DELETE_CIRCLE, CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION,
                    APPROVE_APPLICATION, DECLINE_APPLICATION, UPDATE_PROFILE, UPDATE_PROFILE_LOCATION,
                    CREATE_RIDE, JOIN_RIDE, UPDATE_RIDE, LEAVE_RIDE, CANCEL_RIDE, START_RIDE, COMPLETE_RIDE -> 0.85d;
            case MARK_CHAT_READ, MARK_NOTIFICATIONS_READ, MARK_NOTIFICATION_READ, UPDATE_NOTIFICATION_PREFERENCES, RELEASE_WORKER, REPLACE_WORKER, REOPEN_QUEST, CANCEL_QUEST, PAUSE_QUEST, RESUME_QUEST, RESCHEDULE_BOOKING, CREATE_THING, REQUEST_BORROW, CANCEL_BORROW, DECIDE_BORROW, RETURN_BORROW -> 0.85d;
            case VIEW_NOTIFICATIONS, VIEW_QUEST_NEWS -> 0.70d;
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY, VIEW_BUSINESS_BOOKINGS -> 0.70d;
            case VIEW_THINGS, VIEW_BORROW_REQUESTS -> 0.70d;
            case EDIT_CHAT_MESSAGE, REPLY_TO_CHAT_MESSAGE, REACT_TO_CHAT_MESSAGE -> 0.85d;
            case CREATE_BUSINESS_PROFILE, UPDATE_BUSINESS_PROFILE -> 0.85d;
            case CREATE_GALLERY_IMAGE, UPDATE_GALLERY_IMAGE, DELETE_GALLERY_IMAGE -> 0.85d;
            case LEAVE_CIRCLE -> 0.85d;
            case UPDATE_THING, ARCHIVE_THING -> 0.85d;
            case CREATE_AVAILABILITY_RULE, UPDATE_AVAILABILITY_RULE, DELETE_AVAILABILITY_RULE, CREATE_AVAILABILITY_EXCEPTION, UPDATE_AVAILABILITY_EXCEPTION, DELETE_AVAILABILITY_EXCEPTION -> 0.85d;
            case CONFIRM_BOOKING, CANCEL_BOOKING -> 0.85d;
            case REJECT_BOOKING, COMPLETE_BOOKING, MARK_BOOKING_NO_SHOW -> 0.85d;
            case ARCHIVE_OFFERING -> 0.85d;
            case UPDATE_QUEST -> 0.85d;
            case CREATE_OFFERING, UPDATE_OFFERING -> 0.85d;
            case CREATE_BOOKING -> 0.85d;
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
                    VIEW_CIRCLE_DETAIL, VIEW_ACCESSIBLE_CIRCLE, VIEW_QUEST_DETAIL, VIEW_APPLICATION_DETAIL -> true;
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
            case OPEN_CHAT, VIEW_USER_PROFILE, VIEW_CIRCLE_DETAIL, VIEW_ACCESSIBLE_CIRCLE, VIEW_QUEST_DETAIL, VIEW_APPLICATION_DETAIL -> 0.75d;
            case SEARCH -> 0.75d;
            case VIEW_NOTIFICATIONS, VIEW_QUEST_NEWS -> 0.70d;
            case VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY, VIEW_BUSINESS_BOOKINGS -> 0.70d;
            case VIEW_THINGS, VIEW_BORROW_REQUESTS -> 0.70d;
            case EDIT_CHAT_MESSAGE, REPLY_TO_CHAT_MESSAGE, REACT_TO_CHAT_MESSAGE -> 0.85d;
            case CREATE_BUSINESS_PROFILE, UPDATE_BUSINESS_PROFILE -> 0.85d;
            case CREATE_GALLERY_IMAGE, UPDATE_GALLERY_IMAGE, DELETE_GALLERY_IMAGE -> 0.85d;
            case CONFIRM_BOOKING, CANCEL_BOOKING -> 0.85d;
            case REJECT_BOOKING, COMPLETE_BOOKING, MARK_BOOKING_NO_SHOW -> 0.85d;
            case ARCHIVE_OFFERING -> 0.85d;
            case UPDATE_QUEST -> 0.85d;
            case CREATE_OFFERING, UPDATE_OFFERING -> 0.85d;
            case CREATE_BOOKING -> 0.85d;
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

    private VisionSemanticRouteDescriptor rideRoute(String key, String intent, String capability, String purpose, boolean mutating) {
        boolean create = "CREATE_RIDE".equals(intent);
        return VisionSemanticRouteDescriptor.builder().routeKey(key).entityType("ride").intent(intent).capabilityId(capability)
                .purpose(purpose).mutating(mutating).requiresReview(mutating)
                .examples(create ? List.of(example("offer a ride from Zug to Zurich at 2099-07-20T10:00:00Z", Map.of())) : List.of(example(intent.toLowerCase(Locale.ROOT).replace('_', ' ') + " ride 42", Map.of("ride_id", "42"))))
                .slots(create ? List.of(slot("ride_origin", "ride.origin", "short_text", true, "Ride origin.", List.of(), List.of("from", "origin"), List.of()), slot("ride_destination", "ride.destination", "short_text", true, "Ride destination.", List.of(), List.of("to", "destination"), List.of()), slot("ride_departure_at", "ride.departureAt", "datetime", true, "Future departure time.", List.of(), List.of("at", "departure"), List.of()), slot("ride_seats", "ride.seats", "integer", false, "Passenger seat count.", List.of("1", "8"), List.of("seats", "passengers"), List.of())) : List.of(slot("ride_id", "ride.id", "identifier", true, "Ride id.", List.of(), List.of("ride", "offer", "trip"), List.of())))
                .build();
    }

    private VisionSemanticRouteDescriptor viewThingDetailRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_thing_detail")
                .entityType("thing")
                .intent("VIEW_THING_DETAIL")
                .capabilityId("view_thing_detail")
                .purpose("Read-only detailed snapshot of one authorized shared thing listing; the Web client opens things.detail at /things/{listingId} only after backend target resolution.")
                .mutating(false)
                .requiresReview(false)
                .slots(List.of(slot("thing_listing_id", "thingListing.id", "identifier", true,
                        "Listing id of the shared thing.", List.of(), List.of("thing", "listing", "id"), List.of())))
                .examples(List.of(example("show thing 42", Map.of("thing_listing_id", "42"))))
                .build();
    }

    private VisionSemanticRouteDescriptor viewBorrowRequestsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_borrow_requests")
                .entityType("borrow_request")
                .intent("VIEW_BORROW_REQUESTS")
                .capabilityId("view_borrow_requests")
                .purpose("Read-only list of the authenticated user's Things borrow requests and loans; the Web client opens things.borrow at /things/requests.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(example("show my borrow requests", Map.of()), example("show my loans", Map.of())))
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
                .purpose("Open an existing permitted chat boundary with a contact identified by the user; the Web action opens chat.conversation at /chat/{conversationId} only after backend conversation resolution.")
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
                .purpose("Read-only summary of the authenticated user's chat workspace; the Web action opens chat.workspace at /chat when no specific authorized conversation is resolved.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("show chat", Map.of()),
                        example("show my chat workspace", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor syncChatRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.sync_chat")
                .entityType("chat")
                .intent("SYNC_CHAT")
                .capabilityId("sync_chat")
                .purpose("Refresh the currently opened permitted chat conversation after a disconnected or stale client session.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("refresh this chat", Map.of()),
                        example("recover the latest messages", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewChatAttachmentRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_chat_attachment")
                .entityType("chat_message")
                .intent("VIEW_CHAT_ATTACHMENT")
                .capabilityId("view_chat_attachment")
                .purpose("Read one authorized chat attachment through its backend-issued expiring access URL.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("show attachment 42 in conversation 7", Map.of("chat_conversation_id", "7", "chat_message_id", "42")),
                        example("open chat message 42 attachment in chat 7", Map.of("chat_conversation_id", "7", "chat_message_id", "42"))
                ))
                .slots(List.of(
                        slot("chat_conversation_id", "chatConversationId", "identifier", true, "Authorized chat conversation id.", List.of(), List.of("chat", "conversation", "thread"), List.of()),
                        slot("chat_message_id", "chatMessageId", "identifier", true, "Authorized chat message id containing the attachment.", List.of(), List.of("message", "attachment"), List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor editChatMessageRoute() {
        return chatMessageMutationRoute("vision.edit_chat_message", "EDIT_CHAT_MESSAGE", "edit_chat_message", "Edit an own chat message after explicit confirmation.", "edit message 42 to hello");
    }

    private VisionSemanticRouteDescriptor replyToChatMessageRoute() {
        return chatMessageMutationRoute("vision.reply_to_chat_message", "REPLY_TO_CHAT_MESSAGE", "reply_to_chat_message", "Reply to a chat message after explicit confirmation.", "reply to message 42: thanks");
    }

    private VisionSemanticRouteDescriptor reactToChatMessageRoute() {
        return chatMessageMutationRoute("vision.react_to_chat_message", "REACT_TO_CHAT_MESSAGE", "react_to_chat_message", "Add an emoji reaction to a chat message after explicit confirmation.", "react to message 42 with 👍");
    }

    private VisionSemanticRouteDescriptor chatMessageMutationRoute(String routeKey, String intent, String capabilityId, String purpose, String example) {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey(routeKey).entityType("chat_message").intent(intent).capabilityId(capabilityId)
                .purpose(purpose).mutating(true).requiresReview(true)
                .examples(List.of(example(example, Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor createBusinessProfileRoute() {
        return businessProfileMutationRoute("vision.create_business_profile", "CREATE_BUSINESS_PROFILE", "create_business_profile", "Create a business profile from a reviewed business name.", "create business profile Acme Studio");
    }

    private VisionSemanticRouteDescriptor updateBusinessProfileRoute() {
        return businessProfileMutationRoute("vision.update_business_profile", "UPDATE_BUSINESS_PROFILE", "update_business_profile", "Update the authenticated user's business profile from reviewed fields.", "update business profile name to Acme Studio");
    }

    private VisionSemanticRouteDescriptor businessProfileMutationRoute(String routeKey, String intent, String capabilityId, String purpose, String example) {
        return VisionSemanticRouteDescriptor.builder().routeKey(routeKey).entityType("business_profile").intent(intent).capabilityId(capabilityId)
                .purpose(purpose).mutating(true).requiresReview(true).examples(List.of(example(example, Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor galleryMutationRoute(String routeKey, String intent, String capabilityId, String purpose, String example) {
        return VisionSemanticRouteDescriptor.builder().routeKey(routeKey).entityType("business_gallery_image").intent(intent).capabilityId(capabilityId)
                .purpose(purpose).mutating(true).requiresReview(true).examples(List.of(example(example, Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor membershipMutationRoute(String routeKey, String intent, String capabilityId, String purpose, String example) {
        return VisionSemanticRouteDescriptor.builder().routeKey(routeKey).entityType("circle_membership").intent(intent).capabilityId(capabilityId)
                .purpose(purpose).mutating(true).requiresReview(true).examples(List.of(example(example, Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor thingMutationRoute(String routeKey, String intent, String capabilityId, String purpose, String example) {
        return VisionSemanticRouteDescriptor.builder().routeKey(routeKey).entityType("thing").intent(intent).capabilityId(capabilityId)
                .purpose(purpose).mutating(true).requiresReview(true).examples(List.of(example(example, Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor availabilityMutationRoute(String routeKey, String intent, String capabilityId, String purpose, String example) {
        return VisionSemanticRouteDescriptor.builder().routeKey(routeKey).entityType("business_availability").intent(intent).capabilityId(capabilityId)
                .purpose(purpose).mutating(true).requiresReview(true).examples(List.of(example(example, Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor confirmBookingRoute() {
        return bookingMutationRoute("vision.confirm_booking", "CONFIRM_BOOKING", "confirm_booking", "Confirm an owner booking after explicit confirmation.", "confirm booking 42");
    }

    private VisionSemanticRouteDescriptor cancelBookingRoute() {
        return bookingMutationRoute("vision.cancel_booking", "CANCEL_BOOKING", "cancel_booking", "Cancel an owned or customer booking after explicit confirmation.", "cancel booking 42");
    }

    private VisionSemanticRouteDescriptor bookingMutationRoute(String routeKey, String intent, String capabilityId, String purpose, String example) {
        return VisionSemanticRouteDescriptor.builder().routeKey(routeKey).entityType("booking").intent(intent).capabilityId(capabilityId)
                .purpose(purpose).mutating(true).requiresReview(true).examples(List.of(example(example, Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor rejectBookingRoute() {
        return bookingMutationRoute("vision.reject_booking", "REJECT_BOOKING", "reject_booking", "Reject an owner booking request after explicit confirmation.", "reject booking 42");
    }

    private VisionSemanticRouteDescriptor completeBookingRoute() {
        return bookingMutationRoute("vision.complete_booking", "COMPLETE_BOOKING", "complete_booking", "Complete an owner appointment after explicit confirmation.", "complete booking 42");
    }

    private VisionSemanticRouteDescriptor markBookingNoShowRoute() {
        return bookingMutationRoute("vision.mark_booking_no_show", "MARK_BOOKING_NO_SHOW", "mark_booking_no_show", "Mark an owner appointment as no-show after explicit confirmation.", "mark booking 42 no show");
    }

    private VisionSemanticRouteDescriptor archiveOfferingRoute() {
        return VisionSemanticRouteDescriptor.builder().routeKey("vision.archive_offering").entityType("business_offering")
                .intent("ARCHIVE_OFFERING").capabilityId("archive_offering")
                .purpose("Archive an owned business offering after explicit confirmation.").mutating(true).requiresReview(true)
                .examples(List.of(example("archive offering 42", Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor updateQuestRoute() {
        return VisionSemanticRouteDescriptor.builder().routeKey("vision.update_quest").entityType("quest")
                .intent("UPDATE_QUEST").capabilityId("update_quest")
                .purpose("Update the title of an owned quest after explicit confirmation.").mutating(true).requiresReview(true)
                .examples(List.of(example("rename quest 42 to Move sofa", Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor createOfferingRoute() {
        return offeringMutationRoute("vision.create_offering", "CREATE_OFFERING", "create_offering", "Create a business offering from a reviewed title.", "create offering haircut");
    }

    private VisionSemanticRouteDescriptor updateOfferingRoute() {
        return offeringMutationRoute("vision.update_offering", "UPDATE_OFFERING", "update_offering", "Update an owned business offering title after review.", "rename offering 42 to haircut");
    }

    private VisionSemanticRouteDescriptor offeringMutationRoute(String routeKey, String intent, String capabilityId, String purpose, String example) {
        return VisionSemanticRouteDescriptor.builder().routeKey(routeKey).entityType("business_offering").intent(intent).capabilityId(capabilityId)
                .purpose(purpose).mutating(true).requiresReview(true).examples(List.of(example(example, Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor createBookingRoute() {
        return VisionSemanticRouteDescriptor.builder().routeKey("vision.create_booking").entityType("booking")
                .intent("CREATE_BOOKING").capabilityId("create_booking")
                .purpose("Request an appointment for an offering after explicit confirmation.").mutating(true).requiresReview(true)
                .examples(List.of(example("book offering 42 at 2026-08-01T10:00:00Z until 2026-08-01T11:00:00Z", Map.of()))).slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor rescheduleBookingRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.reschedule_booking")
                .entityType("booking")
                .intent("RESCHEDULE_BOOKING")
                .capabilityId("reschedule_booking")
                .purpose("Reschedule an owned or customer booking after explicit confirmation and backend availability validation.")
                .mutating(true).requiresReview(true)
                .examples(List.of(example("reschedule booking 42 to 2026-08-01T10:00:00Z to 2026-08-01T11:00:00Z", Map.of())))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor markChatReadRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.mark_chat_read")
                .entityType("chat")
                .intent("MARK_CHAT_READ")
                .capabilityId("mark_chat_read")
                .purpose("Mark the currently opened permitted chat conversation as read after explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example("mark this chat as read", Map.of()),
                        example("clear the unread messages in this chat", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor markNotificationsReadRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.mark_notifications_read")
                .entityType("notification")
                .intent("MARK_NOTIFICATIONS_READ")
                .capabilityId("mark_notifications_read")
                .purpose("Mark all current user notifications as read after explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example("mark all notifications as read", Map.of()),
                        example("clear my unread notifications", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor markNotificationReadRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.mark_notification_read")
                .entityType("notification")
                .intent("MARK_NOTIFICATION_READ")
                .capabilityId("mark_notification_read")
                .purpose("Mark one identified notification as read after explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example("mark notification 42 as read", Map.of()),
                        example("clear notification #42", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor updateNotificationPreferencesRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.update_notification_preferences")
                .entityType("notification_preferences")
                .intent("UPDATE_NOTIFICATION_PREFERENCES")
                .capabilityId("update_notification_preferences")
                .purpose("Enable or disable one notification delivery preference after review and explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example("disable chat push notifications", Map.of()),
                        example("enable work email notifications", Map.of())
                ))
                .slots(List.of(
                        slot("notification_category", "notification.category", "enum", true, "Notification category.", List.of("CHAT", "WORK", "BOOKING", "CIRCLE", "LOCATION", "SYSTEM"), List.of("chat", "work", "booking", "circle", "location", "system"), List.of("push", "email", "in-app")),
                        slot("notification_level", "notification.level", "enum", true, "Notification delivery channel.", List.of("IN_APP", "PUSH", "EMAIL"), List.of("in-app", "push", "email"), List.of("chat", "work")),
                        slot("notification_enabled", "notification.enabled", "boolean", true, "Whether the channel should be enabled.", List.of("true", "false"), List.of("enable", "disable", "on", "off", "mute", "unmute"), List.of())
                ))
                .build();
    }

    private VisionSemanticRouteDescriptor workerManagementRoute(boolean replace) {
        String action = replace ? "replace" : "release";
        String intent = replace ? "REPLACE_WORKER" : "RELEASE_WORKER";
        String capability = action + "_worker";
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision." + capability)
                .entityType("quest_worker")
                .intent(intent)
                .capabilityId(capability)
                .purpose((replace ? "Replace" : "Release") + " an assigned quest worker after explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example(action + " worker application 42 from quest 10", Map.of()),
                        example(action + " worker 42 on quest 10", Map.of())
                ))
                .slots(replace
                        ? List.of(
                        slot("worker_quest_id", "quest.id", "integer", true, "Quest id.", List.of(), List.of("quest", "work", "job"), List.of()),
                        slot("worker_application_id", "worker.application_id", "integer", true, "Current approved worker application id.", List.of(), List.of("worker", "application", "assignment"), List.of()),
                        slot("replacement_application_id", "replacement.application_id", "integer", true, "Pending replacement application id.", List.of(), List.of("replacement", "new applicant"), List.of()))
                        : List.of(
                        slot("worker_quest_id", "quest.id", "integer", true, "Quest id.", List.of(), List.of("quest", "work", "job"), List.of()),
                        slot("worker_application_id", "worker.application_id", "integer", true, "Current approved worker application id.", List.of(), List.of("worker", "application", "assignment"), List.of())))
                .build();
    }

    private VisionSemanticRouteDescriptor reopenQuestRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.reopen_quest")
                .entityType("quest")
                .intent("REOPEN_QUEST")
                .capabilityId("reopen_quest")
                .purpose("Reopen an assigned quest owned by the authenticated user after explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example("reopen quest 42", Map.of()),
                        example("make quest #42 open again", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor cancelQuestRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.cancel_quest")
                .entityType("quest")
                .intent("CANCEL_QUEST")
                .capabilityId("cancel_quest")
                .purpose("Cancel an active quest owned by the authenticated user after explicit confirmation and participant notification.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example("cancel quest 42", Map.of()),
                        example("cancel my work #42", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor createThingRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.create_thing")
                .entityType("thing")
                .intent("CREATE_THING")
                .capabilityId("create_thing")
                .purpose("Create a thing listing owned by the authenticated user after explicit review and confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example("offer my drill", Map.of()),
                        example("create a thing listing for a camping tent", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor pauseQuestRoute(boolean resume) {
        String action = resume ? "resume" : "pause";
        String intent = resume ? "RESUME_QUEST" : "PAUSE_QUEST";
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision." + action + "_quest")
                .entityType("quest")
                .intent(intent)
                .capabilityId(action + "_quest")
                .purpose((resume ? "Resume" : "Pause") + " an owned quest after explicit confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example(action + " quest 42", Map.of()),
                        example(action + " my work #42", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor requestBorrowRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.request_borrow")
                .entityType("thing")
                .intent("REQUEST_BORROW")
                .capabilityId("request_borrow")
                .purpose("Request to borrow an available thing listing after explicit review and confirmation.")
                .mutating(true)
                .requiresReview(true)
                .examples(List.of(
                        example("request to borrow thing 42", Map.of()),
                        example("can I borrow listing #42", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor cancelBorrowRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.cancel_borrow")
                .entityType("thing")
                .intent("CANCEL_BORROW")
                .capabilityId("cancel_borrow")
                .purpose("Cancel a pending borrow request owned by the authenticated borrower after explicit confirmation.")
                .mutating(true).requiresReview(true)
                .examples(List.of(example("cancel borrow request 42", Map.of())))
                .slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor decideBorrowRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.decide_borrow")
                .entityType("thing")
                .intent("DECIDE_BORROW")
                .capabilityId("decide_borrow")
                .purpose("Approve or decline a pending borrow request as the listing owner after explicit confirmation.")
                .mutating(true).requiresReview(true)
                .examples(List.of(
                        example("approve borrow request 42", Map.of()),
                        example("decline borrow request 42", Map.of())
                ))
                .slots(List.of()).build();
    }

    private VisionSemanticRouteDescriptor returnBorrowRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.return_borrow")
                .entityType("thing")
                .intent("RETURN_BORROW")
                .capabilityId("return_borrow")
                .purpose("Mark an approved borrowed thing as returned after explicit confirmation.")
                .mutating(true).requiresReview(true)
                .examples(List.of(example("mark borrow request 42 returned", Map.of())))
                .slots(List.of()).build();
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

    private VisionSemanticRouteDescriptor viewBusinessRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_business")
                .entityType("business")
                .intent("VIEW_BUSINESS")
                .capabilityId("view_business")
                .dtoType("BusinessPublicPageDTO")
                .purpose("Read-only Business discovery, public profile, or owner profile handoff; the backend selects /business/find, /business/public/{slug}, or /business/profile from authorized context.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("show my business", Map.of()),
                        example("open business page", Map.of()),
                        example("find a business", Map.of()),
                        example("discover businesses", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewBusinessAvailabilityRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_business_availability")
                .entityType("business")
                .intent("VIEW_BUSINESS_AVAILABILITY")
                .capabilityId("view_business_availability")
                .dtoType("BusinessOwnerDashboardDTO")
                .purpose("Read-only business availability and owner schedule snapshot; the backend-published Web action opens /business/calendar and keeps exception/booking mutations behind existing confirmation boundaries.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("show business schedule", Map.of()),
                        example("open business availability", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewBusinessBookingsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_business_bookings")
                .entityType("business")
                .intent("VIEW_BUSINESS_BOOKINGS")
                .capabilityId("view_business_bookings")
                .dtoType("BusinessBookingListResponseDTO")
                .purpose("Read-only owner or customer booking surface; the backend selects /business/bookings or /business/my-bookings from authorized viewer context.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("show my bookings", Map.of()),
                        example("open business appointments", Map.of())
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
                .purpose("Read-only detailed snapshot of one privacy-authorized user profile; the Web client opens the backend-resolved people.profile action at /people/{userId}.")
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

    private VisionSemanticRouteDescriptor viewAccessibleCircleRoute() {
        return VisionSemanticRouteDescriptor.builder().routeKey("vision.view_accessible_circle").entityType("circle")
                .intent("VIEW_ACCESSIBLE_CIRCLE").capabilityId("view_accessible_circle")
                .purpose("Read-only detail of a circle where the authenticated user is an owner or member.")
                .mutating(false).requiresReview(false)
                .slots(List.of(slot("accessible_circle_id", "circle.id", "identifier", true, "Accessible circle id.", List.of(), List.of("circle", "id"), List.of())))
                .examples(List.of(example("show accessible circle 42", Map.of("accessible_circle_id", "42")))).build();
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

    private VisionSemanticRouteDescriptor viewMyWorkRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_my_work")
                .entityType("quest")
                .intent("VIEW_MY_WORK")
                .capabilityId("work.quest.view_own")
                .purpose("Read-only owner-scoped list of work opportunities posted by the authenticated user, with a canonical Web handoff to My Work.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("show my posted work", Map.of()),
                        example("show me the jobs I posted", Map.of()),
                        example("pokaži mi poslove koje sam ja postao", Map.of()),
                        example("open my work", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewNotificationsRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_notifications")
                .entityType("notification")
                .intent("VIEW_NOTIFICATIONS")
                .capabilityId("view_notifications")
                .purpose("Read-only notifications inbox for the authenticated user; the Web action opens /notifications and preserves viewer scope, with optional unread-only surface state.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("show notifications", Map.of()),
                        example("open notifications", Map.of())
                ))
                .slots(List.of())
                .build();
    }

    private VisionSemanticRouteDescriptor viewActivityRoute() {
        return VisionSemanticRouteDescriptor.builder()
                .routeKey("vision.view_activity")
                .entityType("activity")
                .intent("VIEW_ACTIVITY")
                .capabilityId("view_activity")
                .purpose("Read-only viewer-scoped activity and resumable Vision tasks; the Web action opens /activity without moving read-state or permission rules into Vue.")
                .mutating(false)
                .requiresReview(false)
                .examples(List.of(
                        example("show my activity", Map.of()),
                        example("what should I continue", Map.of()),
                        example("show recent history", Map.of())
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
                .purpose("Read-only summary of the authenticated user's quest applications; the Web client opens the backend-published work.applications action at /work/applications.")
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
