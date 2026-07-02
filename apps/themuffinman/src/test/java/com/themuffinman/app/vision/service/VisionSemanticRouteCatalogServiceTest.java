package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionSemanticRouteCatalogServiceTest {

    private final VisionSemanticRouteCatalogService service = new VisionSemanticRouteCatalogService();

    @Test
    void publishesCurrentVisionRoutesForAuthenticatedUser() {
        AppUser user = new AppUser();
        user.setId(3L);

        var routes = service.allowedRoutes(user);

        assertEquals(25, routes.size());
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.create_circle")
                && route.isMutating()
                && route.isRequiresReview()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("circle_name"))));
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
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_user_profile")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_user"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_circles")
                && !route.isMutating()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_circle_detail")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_circle_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_quest_detail")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_quest_query"))));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_applications")
                && !route.isMutating()
                && route.getSlots().isEmpty()));
        assertTrue(routes.stream().anyMatch(route -> route.getRouteKey().equals("vision.view_application_detail")
                && !route.isMutating()
                && route.getSlots().stream().anyMatch(slot -> slot.getSlotId().equals("target_application_query"))));
    }

    @Test
    void publishesNoRoutesWithoutAuthenticatedUser() {
        assertFalse(service.allowedRoutes(null).iterator().hasNext());
    }
}
