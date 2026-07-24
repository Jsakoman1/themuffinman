package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.vision.model.VisionIntent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionSemanticRouteCatalogServiceTest {

    private final VisionSemanticRouteCatalogService service = new VisionSemanticRouteCatalogService();

    @Test
    void publishesCurrentVisionRoutesForAuthenticatedUser() {
        AppUser user = new AppUser();
        user.setId(3L);

        var routes = service.allowedRoutes(user);

        assertEquals(91, routes.size());
        assertEquals(SemanticEntityFamily.QUEST, service.entityFamilyForIntent(VisionIntent.CREATE_QUEST));
        assertEquals(SemanticEntityFamily.NOTIFICATIONS, service.entityFamilyForIntent(VisionIntent.VIEW_NOTIFICATIONS));
        assertEquals(SemanticEntityFamily.QUEST, service.entityFamilyForIntent(VisionIntent.VIEW_QUEST_NEWS));
        assertEquals(SemanticEntityFamily.QUEST, service.entityFamilyForIntent(VisionIntent.VIEW_MY_WORK));
        assertEquals(SemanticEntityFamily.BUSINESS, service.entityFamilyForIntent(VisionIntent.VIEW_BUSINESS));
        assertEquals(SemanticEntityFamily.BUSINESS, service.entityFamilyForIntent(VisionIntent.VIEW_BUSINESS_AVAILABILITY));
        assertEquals("QuestRequestDTO", service.dtoTypeForIntent(VisionIntent.CREATE_QUEST));
        assertEquals("DashboardNotificationsSectionDTO", service.dtoTypeForIntent(VisionIntent.VIEW_NOTIFICATIONS));
        assertEquals("ThingListingListResponseDTO", service.dtoTypeForIntent(VisionIntent.VIEW_THINGS));
        assertEquals("BusinessPublicPageDTO", service.dtoTypeForIntent(VisionIntent.VIEW_BUSINESS));
        assertEquals("BusinessOwnerDashboardDTO", service.dtoTypeForIntent(VisionIntent.VIEW_BUSINESS_AVAILABILITY));
        assertTrue(service.routeForCapabilityId("view_business").getPurpose().contains("schema"));
        assertTrue(service.routeForCapabilityId("view_business").getPurpose().contains("availability"));
        assertEquals("create_quest_validator", service.validatorKeyForIntent(VisionIntent.CREATE_QUEST));
        assertEquals("create_quest_executor", service.executorKeyForIntent(VisionIntent.CREATE_QUEST));
        assertEquals(0.85d, service.minimumConfidenceForIntent(VisionIntent.CREATE_QUEST));
        assertEquals(0.70d, service.minimumConfidenceForIntent(VisionIntent.VIEW_NOTIFICATIONS));
        assertEquals(0.70d, service.minimumConfidenceForIntent(VisionIntent.VIEW_BUSINESS));
        assertEquals(0.70d, service.minimumConfidenceForIntent(VisionIntent.VIEW_THINGS));
        assertEquals(0.75d, service.minimumConfidenceForIntent(VisionIntent.SEARCH));
        assertTrue(service.requiresTargetEntityResolution(VisionIntent.CREATE_CIRCLE_REQUEST));
        assertEquals(0.88d, service.minimumEntityResolutionConfidenceForIntent(VisionIntent.CREATE_CIRCLE_REQUEST));
        assertFalse(service.requiresTargetEntityResolution(VisionIntent.CREATE_QUEST));
        assertFalse(service.requiresTargetEntityResolution(VisionIntent.VIEW_NOTIFICATIONS));
        assertFalse(service.requiresTargetEntityResolution(VisionIntent.VIEW_THINGS));
        assertFalse(service.requiresTargetEntityResolution(VisionIntent.SEARCH));
        assertEquals(List.of("quest_title", "quest_description", "reward_amount", "schedule_mode", "visibility", "location_mode"),
                service.requiredSlotIdsForIntent(VisionIntent.CREATE_QUEST));
        assertEquals(List.of("target_user"), service.requiredSlotIdsForCapabilityId("create_circle_request"));
        assertEquals(
                List.of(
                        "CREATE_QUEST",
                        "CREATE_CIRCLE",
                        "CREATE_CIRCLE_REQUEST",
                        "ACCEPT_CIRCLE_REQUEST",
                        "DELETE_CIRCLE_REQUEST",
                        "UPDATE_CIRCLE",
                        "DELETE_CIRCLE",
                        "LEAVE_CIRCLE",
                        "CREATE_APPLICATION",
                        "UPDATE_APPLICATION",
                        "WITHDRAW_APPLICATION",
                        "APPROVE_APPLICATION",
                        "DECLINE_APPLICATION",
                        "UPDATE_PROFILE",
                        "UPDATE_PROFILE_LOCATION",
                        "SEARCH",
                        "DISCOVER_QUESTS",
                        "VIEW_THINGS",
                        "VIEW_THING_DETAIL",
                        "VIEW_BORROW_REQUESTS",
                        "OPEN_CHAT",
                        "SEND_MESSAGE",
                        "VIEW_CHAT_WORKSPACE",
                        "SYNC_CHAT",
                        "VIEW_CHAT_ATTACHMENT",
                        "EDIT_CHAT_MESSAGE",
                        "REPLY_TO_CHAT_MESSAGE",
                        "REACT_TO_CHAT_MESSAGE",
                        "CREATE_BUSINESS_PROFILE",
                        "UPDATE_BUSINESS_PROFILE",
                        "CREATE_GALLERY_IMAGE",
                        "UPDATE_GALLERY_IMAGE",
                        "DELETE_GALLERY_IMAGE",
                        "CREATE_AVAILABILITY_RULE",
                        "UPDATE_AVAILABILITY_RULE",
                        "DELETE_AVAILABILITY_RULE",
                        "CREATE_AVAILABILITY_EXCEPTION",
                        "UPDATE_AVAILABILITY_EXCEPTION",
                        "DELETE_AVAILABILITY_EXCEPTION",
                        "CONFIRM_BOOKING",
                        "CANCEL_BOOKING",
                        "REJECT_BOOKING",
                        "COMPLETE_BOOKING",
                        "MARK_BOOKING_NO_SHOW",
                        "ARCHIVE_OFFERING",
                        "UPDATE_QUEST",
                        "CREATE_OFFERING",
                        "UPDATE_OFFERING",
                        "CREATE_BOOKING",
                        "RESCHEDULE_BOOKING",
                        "MARK_CHAT_READ",
                        "MARK_NOTIFICATIONS_READ",
                        "MARK_NOTIFICATION_READ",
                        "UPDATE_NOTIFICATION_PREFERENCES",
                        "RELEASE_WORKER",
                        "REPLACE_WORKER",
                        "REOPEN_QUEST",
                        "CANCEL_QUEST",
                        "PAUSE_QUEST",
                        "RESUME_QUEST",
                        "CREATE_THING",
                        "REQUEST_BORROW",
                        "CANCEL_BORROW",
                        "DECIDE_BORROW",
                        "RETURN_BORROW",
                        "CREATE_RIDE",
                        "VIEW_RIDES",
                        "JOIN_RIDE",
                        "UPDATE_RIDE",
                        "LEAVE_RIDE",
                        "CANCEL_RIDE",
                        "START_RIDE",
                        "COMPLETE_RIDE",
                        "UPDATE_THING",
                        "ARCHIVE_THING",
                        "VIEW_PROFILE",
                        "VIEW_SETTINGS",
                        "VIEW_BUSINESS",
                        "VIEW_BUSINESS_AVAILABILITY",
                        "VIEW_BUSINESS_BOOKINGS",
                        "VIEW_USER_PROFILE",
                        "VIEW_CIRCLES",
                        "VIEW_CIRCLE_DETAIL",
                        "VIEW_ACCESSIBLE_CIRCLE",
                        "VIEW_QUEST_DETAIL",
                        "VIEW_MY_WORK",
                        "VIEW_NOTIFICATIONS",
                        "VIEW_ACTIVITY",
                        "VIEW_QUEST_NEWS",
                        "VIEW_APPLICATIONS",
                        "VIEW_APPLICATION_DETAIL",
                        "UNSUPPORTED"
                ),
                service.supportedCandidateIntents()
        );
        assertEquals(
                List.of(
                        "create_quest",
                        "create_circle",
                        "create_circle_request",
                        "accept_circle_request",
                        "delete_circle_request",
                        "update_circle",
                        "delete_circle",
                        "leave_circle",
                        "create_application",
                        "update_application",
                        "withdraw_application",
                        "approve_application",
                        "decline_application",
                        "update_profile",
                        "update_profile_location",
                        "search",
                        "discover_quests",
                        "view_things",
                        "view_thing_detail",
                        "view_borrow_requests",
                        "open_chat",
                        "send_message",
                        "view_chat_workspace",
                        "sync_chat",
                        "view_chat_attachment",
                        "edit_chat_message",
                        "reply_to_chat_message",
                        "react_to_chat_message",
                        "create_business_profile",
                        "update_business_profile",
                        "create_gallery_image",
                        "update_gallery_image",
                        "delete_gallery_image",
                        "create_availability_rule",
                        "update_availability_rule",
                        "delete_availability_rule",
                        "create_availability_exception",
                        "update_availability_exception",
                        "delete_availability_exception",
                        "confirm_booking",
                        "cancel_booking",
                        "reject_booking",
                        "complete_booking",
                        "mark_booking_no_show",
                        "archive_offering",
                        "update_quest",
                        "create_offering",
                        "update_offering",
                        "create_booking",
                        "reschedule_booking",
                        "mark_chat_read",
                        "mark_notifications_read",
                        "mark_notification_read",
                        "update_notification_preferences",
                        "release_worker",
                        "replace_worker",
                        "reopen_quest",
                        "cancel_quest",
                        "pause_quest",
                        "resume_quest",
                        "create_thing",
                        "request_borrow",
                        "cancel_borrow",
                        "decide_borrow",
                        "return_borrow",
                        "create_ride",
                        "view_rides",
                        "join_ride",
                        "update_ride",
                        "leave_ride",
                        "cancel_ride",
                        "start_ride",
                        "complete_ride",
                        "update_thing",
                        "archive_thing",
                        "view_profile",
                        "view_settings",
                        "view_business",
                        "view_business_availability",
                        "view_business_bookings",
                        "view_user_profile",
                        "view_circles",
                        "view_circle_detail",
                        "view_accessible_circle",
                        "view_quest_detail",
                        "work.quest.view_own",
                        "view_notifications",
                        "view_activity",
                        "view_quest_news",
                        "view_applications",
                        "view_application_detail",
                        "unsupported"
                ),
                service.supportedCapabilityIds()
        );
        assertNotNull(service.routeForIntent("CREATE_QUEST"));
        assertNotNull(service.routeForIntent("VIEW_NOTIFICATIONS"));
        assertNotNull(service.routeForIntent("VIEW_THINGS"));
        assertNotNull(service.routeForIntent("VIEW_MY_WORK"));
        assertNotNull(service.routeForIntent("VIEW_THING_DETAIL"));
        assertNotNull(service.routeForIntent("VIEW_SETTINGS"));
        assertNotNull(service.routeForIntent("SEARCH"));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.create_circle")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("circle_name"))
                && route.getExamples() != null
                && !route.getExamples().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.search")
                && !route.isMutating()
                && !route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("search_query"))
                && route.getExamples() != null
                && !route.getExamples().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_things")
                && !route.isMutating()
                && !route.isRequiresReview()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.create_circle_request")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().size() == 1
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_user"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.accept_circle_request")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().size() == 1
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_user"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.delete_circle_request")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().size() == 1
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_user"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.create_application")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_quest_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.update_application")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_quest_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.withdraw_application")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().size() == 1
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_quest_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.update_profile")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("profile_username"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.update_circle")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_circle_query"))
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("circle_name"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.delete_circle")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().size() == 1
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_circle_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.approve_application")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_quest_query"))
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_user"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.decline_application")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_quest_query"))
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_user"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.update_profile_location")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("profile_location_mode"))
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("profile_location_label"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.create_quest")
                && route.isMutating()
                && route.isRequiresReview()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.discover_quests")
                && !route.isMutating()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.open_chat")
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_user"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_chat_workspace")
                && !route.isMutating()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_profile")
                && !route.isMutating()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_settings")
                && !route.isMutating()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_business")
                && !route.isMutating()
                && route.getSlots().isEmpty()
                && "BusinessPublicPageDTO".equals(route.getDtoType())));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_business_availability")
                && !route.isMutating()
                && route.getSlots().isEmpty()
                && "BusinessOwnerDashboardDTO".equals(route.getDtoType())));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_business_bookings")
                && !route.isMutating()
                && route.getSlots().isEmpty()
                && "BusinessBookingListResponseDTO".equals(route.getDtoType())));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_settings")
                && "view_settings".equals(route.getCapabilityId())
                && "settings".equals(route.getEntityType())));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_user_profile")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_user"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_circles")
                && !route.isMutating()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_circle_detail")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_circle_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_accessible_circle")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("accessible_circle_id"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_quest_detail")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_quest_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_quest_news")
                && !route.isMutating()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_notifications")
                && !route.isMutating()
                && !route.isRequiresReview()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_quest_news")
                && "view_quest_news".equals(route.getCapabilityId())));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_applications")
                && !route.isMutating()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_application_detail")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_application_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.update_profile")
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("profile_username"))
                && route.getSlots().stream().anyMatch(slot -> slot.getAliases() != null && !slot.getAliases().isEmpty())
                && route.getSlots().stream().anyMatch(slot -> slot.getAntiExamples() != null)));
    }

    @Test
    void routeCatalogKeepsUniqueCapabilitiesAndValidatorMetadataAligned() {
        List<VisionSemanticRouteDescriptor> routes = service.allRoutes();
        Set<String> routeKeys = new HashSet<>();
        Set<String> capabilityIds = new HashSet<>();

        for (VisionSemanticRouteDescriptor route : routes) {
            assertTrue(routeKeys.add(route.getRouteKey()), () -> "Duplicate route key: " + route.getRouteKey());
            assertTrue(capabilityIds.add(route.getCapabilityId()), () -> "Duplicate capability id: " + route.getCapabilityId());
            assertNotNull(route.getIntent(), () -> "Route intent missing for " + route.getRouteKey());
            assertNotNull(route.getCapabilityId(), () -> "Route capability missing for " + route.getRouteKey());
            assertFalse(route.getCapabilityId().isBlank(), () -> "Route capability blank for " + route.getRouteKey());
            assertNotNull(service.validatorKeyForIntent(VisionIntent.valueOf(route.getIntent())));
            assertNotNull(service.executorKeyForIntent(VisionIntent.valueOf(route.getIntent())));
            assertFalse(service.validatorKeyForIntent(VisionIntent.valueOf(route.getIntent())).isBlank(),
                    () -> "Validator key blank for " + route.getRouteKey());
            assertFalse(service.executorKeyForIntent(VisionIntent.valueOf(route.getIntent())).isBlank(),
                    () -> "Executor key blank for " + route.getRouteKey());
            if (route.isMutating()) {
                assertTrue(service.requiresTargetEntityResolution(VisionIntent.valueOf(route.getIntent()))
                                || service.minimumConfidenceForIntent(VisionIntent.valueOf(route.getIntent())) >= 0.85d,
                        () -> "Mutating route should have typed execution or confidence gate metadata: " + route.getRouteKey());
            }
        }

        assertEquals(91, routes.size());
        assertEquals(
                Set.of(
                        "create_quest",
                        "create_circle",
                        "create_circle_request",
                        "accept_circle_request",
                        "delete_circle_request",
                        "create_application",
                        "update_application",
                        "withdraw_application",
                        "approve_application",
                        "decline_application",
                        "update_circle",
                        "delete_circle",
                        "leave_circle",
                        "update_profile",
                        "update_profile_location",
                        "mark_chat_read",
                        "mark_notifications_read"
                        ,"mark_notification_read",
                        "update_notification_preferences",
                        "release_worker",
                        "replace_worker",
                        "reopen_quest",
                        "cancel_quest"
                        ,"pause_quest"
                        ,"resume_quest"
                        ,"create_thing",
                        "request_borrow",
                        "cancel_borrow",
                        "decide_borrow",
                        "return_borrow",
                        "create_ride",
                        "join_ride",
                        "update_ride",
                        "leave_ride",
                        "cancel_ride",
                        "start_ride",
                        "complete_ride",
                        "edit_chat_message",
                        "reply_to_chat_message",
                        "react_to_chat_message"
                        ,"create_business_profile",
                        "update_business_profile",
                        "create_gallery_image",
                        "update_gallery_image",
                        "delete_gallery_image",
                        "update_thing",
                        "archive_thing",
                        "create_availability_rule",
                        "update_availability_rule",
                        "delete_availability_rule",
                        "create_availability_exception",
                        "update_availability_exception",
                        "delete_availability_exception",
                        "confirm_booking",
                        "cancel_booking",
                        "reject_booking",
                        "complete_booking",
                        "mark_booking_no_show",
                        "archive_offering",
                        "update_quest",
                        "create_offering",
                        "update_offering"
                        ,"create_booking",
                        "reschedule_booking"
                ),
                new HashSet<>(new VisionSurfacePolicy(new com.themuffinman.app.config.VisionProperties()).supportedExecutionCapabilityIds())
        );
    }

    @Test
    void publishesNoRoutesWithoutAuthenticatedUser() {
        assertFalse(service.allowedRoutes(null).iterator().hasNext());
    }
}
