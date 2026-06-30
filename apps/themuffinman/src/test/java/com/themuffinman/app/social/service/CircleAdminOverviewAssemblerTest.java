package com.themuffinman.app.social.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.dto.AdminCircleGroupResponseDTO;
import com.themuffinman.app.social.dto.AdminCircleOverviewDTO;
import com.themuffinman.app.social.model.CircleRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CircleAdminOverviewAssemblerTest {

    private final CircleAdminOverviewAssembler circleAdminOverviewAssembler = new CircleAdminOverviewAssembler(
            new SocialPresentationHelper()
    );

    @Test
    void buildAdminOverviewFiltersCirclesAndRelationRowsByQuery() {
        AppUser owner = createUser(1L, "owner");
        AppUser helper = createUser(2L, "helper");
        AppUser requester = createUser(3L, "requester");
        AppUser recipient = createUser(4L, "recipient");
        AppUser otherRecipient = createUser(5L, "other-recipient");

        AdminCircleGroupResponseDTO matchingCircle = AdminCircleGroupResponseDTO.builder()
                .id(10L)
                .name("Helpers")
                .ownerId(owner.getId())
                .ownerUsername(owner.getUsername())
                .memberCount(1)
                .memberPreviewLabel("helper")
                .members(List.of())
                .build();

        AdminCircleGroupResponseDTO nonMatchingCircle = AdminCircleGroupResponseDTO.builder()
                .id(11L)
                .name("Drivers")
                .ownerId(owner.getId())
                .ownerUsername(owner.getUsername())
                .memberCount(0)
                .memberPreviewLabel("none")
                .members(List.of())
                .build();

        CircleRequest matchingPendingRequest = new CircleRequest();
        matchingPendingRequest.setId(20L);
        matchingPendingRequest.setRequester(helper);
        matchingPendingRequest.setRecipient(recipient);

        CircleRequest nonMatchingAcceptedRequest = new CircleRequest();
        nonMatchingAcceptedRequest.setId(21L);
        nonMatchingAcceptedRequest.setRequester(owner);
        nonMatchingAcceptedRequest.setRecipient(otherRecipient);
        nonMatchingAcceptedRequest.setAcceptedAt(Instant.now());

        AdminCircleOverviewDTO result = circleAdminOverviewAssembler.buildAdminOverview(
                List.of(nonMatchingCircle, matchingCircle),
                List.of(matchingPendingRequest, nonMatchingAcceptedRequest),
                "helper"
        );

        assertEquals(1, result.getCircles().size());
        assertEquals("Helpers", result.getCircles().getFirst().getName());
        assertEquals("helper", result.getCircles().getFirst().getMemberPreviewLabel());
        assertEquals(0, result.getAcceptedConnections().size());
        assertEquals(1, result.getPendingRequests().size());
        assertEquals("recipient", result.getPendingRequests().getFirst().getRecipientUsername());
        assertEquals("Pending", result.getPendingRequests().getFirst().getStatusLabel());
    }

    @Test
    void buildAdminOverviewBuildsBlockedRowsWithPresentationFields() {
        AppUser requester = createUser(1L, "requester");
        AppUser recipient = createUser(2L, "recipient");

        CircleRequest blockedRequest = new CircleRequest();
        blockedRequest.setId(22L);
        blockedRequest.setRequester(requester);
        blockedRequest.setRecipient(recipient);
        blockedRequest.setBlockedAt(Instant.now());

        AdminCircleOverviewDTO result = circleAdminOverviewAssembler.buildAdminOverview(
                List.of(),
                List.of(blockedRequest),
                null
        );

        assertEquals(1, result.getBlockedRelations().size());
        assertEquals("Blocked", result.getBlockedRelations().getFirst().getStatusLabel());
        assertEquals("badge--danger", result.getBlockedRelations().getFirst().getStatusBadgeClass());
    }

    private AppUser createUser(Long id, String username) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setEmail(username + "@example.com");
        return appUser;
    }
}
