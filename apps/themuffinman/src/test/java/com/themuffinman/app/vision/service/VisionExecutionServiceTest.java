package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import com.themuffinman.app.vision.testing.VisionSlotStatePresets;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.model.Quest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionExecutionServiceTest {

    @Test
    void centralExecutionGateReturnsStableFailureMetadata() {
        VisionProperties visionProperties = new VisionProperties();
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of()
        );

        VisionExecutionResult missingConversation = executionService.execute(null);

        assertFalse(missingConversation.isExecuted());
        assertEquals("VALIDATION", missingConversation.getFailureCode());
        assertFalse(missingConversation.isRetryable());
    }

    @Test
    void blocksExecutionWhenFeatureFlagIsDisabled() {
        VisionProperties visionProperties = new VisionProperties();
        VisionCreateQuestExecutionAdapter questAdapter = mock(VisionCreateQuestExecutionAdapter.class);
        when(questAdapter.capabilityId()).thenReturn("create_quest");
        VisionCreateCircleExecutionAdapter circleAdapter = mock(VisionCreateCircleExecutionAdapter.class);
        when(circleAdapter.capabilityId()).thenReturn("create_circle");
        VisionUpdateProfileExecutionAdapter profileAdapter = mock(VisionUpdateProfileExecutionAdapter.class);
        when(profileAdapter.capabilityId()).thenReturn("update_profile");
        VisionUpdateProfileLocationExecutionAdapter profileLocationAdapter = mock(VisionUpdateProfileLocationExecutionAdapter.class);
        when(profileLocationAdapter.capabilityId()).thenReturn("update_profile_location");
        VisionCreateCircleRequestExecutionAdapter createCircleRequestAdapter = mock(VisionCreateCircleRequestExecutionAdapter.class);
        when(createCircleRequestAdapter.capabilityId()).thenReturn("create_circle_request");
        VisionAcceptCircleRequestExecutionAdapter acceptCircleRequestAdapter = mock(VisionAcceptCircleRequestExecutionAdapter.class);
        when(acceptCircleRequestAdapter.capabilityId()).thenReturn("accept_circle_request");
        VisionDeleteCircleRequestExecutionAdapter deleteCircleRequestAdapter = mock(VisionDeleteCircleRequestExecutionAdapter.class);
        when(deleteCircleRequestAdapter.capabilityId()).thenReturn("delete_circle_request");
        VisionCreateApplicationExecutionAdapter createApplicationAdapter = mock(VisionCreateApplicationExecutionAdapter.class);
        when(createApplicationAdapter.capabilityId()).thenReturn("create_application");
        VisionUpdateApplicationExecutionAdapter updateApplicationAdapter = mock(VisionUpdateApplicationExecutionAdapter.class);
        when(updateApplicationAdapter.capabilityId()).thenReturn("update_application");
        VisionWithdrawApplicationExecutionAdapter withdrawApplicationAdapter = mock(VisionWithdrawApplicationExecutionAdapter.class);
        when(withdrawApplicationAdapter.capabilityId()).thenReturn("withdraw_application");
        VisionApproveApplicationExecutionAdapter approveApplicationAdapter = mock(VisionApproveApplicationExecutionAdapter.class);
        when(approveApplicationAdapter.capabilityId()).thenReturn("approve_application");
        VisionDeclineApplicationExecutionAdapter declineApplicationAdapter = mock(VisionDeclineApplicationExecutionAdapter.class);
        when(declineApplicationAdapter.capabilityId()).thenReturn("decline_application");
        VisionUpdateCircleExecutionAdapter updateCircleAdapter = mock(VisionUpdateCircleExecutionAdapter.class);
        when(updateCircleAdapter.capabilityId()).thenReturn("update_circle");
        VisionDeleteCircleExecutionAdapter deleteCircleAdapter = mock(VisionDeleteCircleExecutionAdapter.class);
        when(deleteCircleAdapter.capabilityId()).thenReturn("delete_circle");
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(
                        questAdapter,
                        circleAdapter,
                        createCircleRequestAdapter,
                        acceptCircleRequestAdapter,
                        deleteCircleRequestAdapter,
                        createApplicationAdapter,
                        updateApplicationAdapter,
                        withdrawApplicationAdapter,
                        approveApplicationAdapter,
                        declineApplicationAdapter,
                        updateCircleAdapter,
                        deleteCircleAdapter,
                        profileAdapter,
                        profileLocationAdapter
                )
        );

        AppUser user = TestFixtures.user(7L, "vision-user");
        VisionExecutionResult result = executionService.execute(
                VisionConversationTestBuilder.createQuest(1L, user)
                        .status(VisionConversationStatus.REVIEW_READY)
                        .slots(VisionSlotStatePresets.createQuestReviewReadyProfileLocation())
                        .build()
        );

        assertFalse(result.isExecuted());
        assertEquals("Execution is disabled by configuration.", result.getBlockingReason());
        assertEquals("CONFIGURATION", result.getFailureCode());
        assertFalse(result.isRetryable());
    }

    @Test
    void executesConfirmedCreateQuestConversationThroughAdapter() {
        VisionProperties visionProperties = new VisionProperties();
        visionProperties.setExecutionEnabled(true);

        VisionCreateQuestExecutionAdapter adapter = mock(VisionCreateQuestExecutionAdapter.class);
        when(adapter.capabilityId()).thenReturn("create_quest");
        VisionCreateCircleExecutionAdapter circleAdapter = mock(VisionCreateCircleExecutionAdapter.class);
        when(circleAdapter.capabilityId()).thenReturn("create_circle");
        VisionUpdateProfileExecutionAdapter profileAdapter = mock(VisionUpdateProfileExecutionAdapter.class);
        when(profileAdapter.capabilityId()).thenReturn("update_profile");
        VisionUpdateProfileLocationExecutionAdapter profileLocationAdapter = mock(VisionUpdateProfileLocationExecutionAdapter.class);
        when(profileLocationAdapter.capabilityId()).thenReturn("update_profile_location");
        VisionCreateCircleRequestExecutionAdapter createCircleRequestAdapter = mock(VisionCreateCircleRequestExecutionAdapter.class);
        when(createCircleRequestAdapter.capabilityId()).thenReturn("create_circle_request");
        VisionAcceptCircleRequestExecutionAdapter acceptCircleRequestAdapter = mock(VisionAcceptCircleRequestExecutionAdapter.class);
        when(acceptCircleRequestAdapter.capabilityId()).thenReturn("accept_circle_request");
        VisionDeleteCircleRequestExecutionAdapter deleteCircleRequestAdapter = mock(VisionDeleteCircleRequestExecutionAdapter.class);
        when(deleteCircleRequestAdapter.capabilityId()).thenReturn("delete_circle_request");
        VisionCreateApplicationExecutionAdapter createApplicationAdapter = mock(VisionCreateApplicationExecutionAdapter.class);
        when(createApplicationAdapter.capabilityId()).thenReturn("create_application");
        VisionUpdateApplicationExecutionAdapter updateApplicationAdapter = mock(VisionUpdateApplicationExecutionAdapter.class);
        when(updateApplicationAdapter.capabilityId()).thenReturn("update_application");
        VisionWithdrawApplicationExecutionAdapter withdrawApplicationAdapter = mock(VisionWithdrawApplicationExecutionAdapter.class);
        when(withdrawApplicationAdapter.capabilityId()).thenReturn("withdraw_application");
        VisionApproveApplicationExecutionAdapter approveApplicationAdapter = mock(VisionApproveApplicationExecutionAdapter.class);
        when(approveApplicationAdapter.capabilityId()).thenReturn("approve_application");
        VisionDeclineApplicationExecutionAdapter declineApplicationAdapter = mock(VisionDeclineApplicationExecutionAdapter.class);
        when(declineApplicationAdapter.capabilityId()).thenReturn("decline_application");
        VisionUpdateCircleExecutionAdapter updateCircleAdapter = mock(VisionUpdateCircleExecutionAdapter.class);
        when(updateCircleAdapter.capabilityId()).thenReturn("update_circle");
        VisionDeleteCircleExecutionAdapter deleteCircleAdapter = mock(VisionDeleteCircleExecutionAdapter.class);
        when(deleteCircleAdapter.capabilityId()).thenReturn("delete_circle");
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(
                        adapter,
                        circleAdapter,
                        createCircleRequestAdapter,
                        acceptCircleRequestAdapter,
                        deleteCircleRequestAdapter,
                        createApplicationAdapter,
                        updateApplicationAdapter,
                        withdrawApplicationAdapter,
                        approveApplicationAdapter,
                        declineApplicationAdapter,
                        updateCircleAdapter,
                        deleteCircleAdapter,
                        profileAdapter,
                        profileLocationAdapter
                )
        );

        AppUser user = TestFixtures.user(7L, "vision-user");
        Quest createdQuest = new Quest();
        createdQuest.setId(501L);

        VisionConversationTestBuilder builder = VisionConversationTestBuilder.createQuest(2L, user)
                .status(VisionConversationStatus.REVIEW_READY)
                .slots(VisionSlotStatePresets.createQuestReviewReadyProfileLocation());
        var conversation = builder.build();
        when(adapter.execute(conversation)).thenReturn(VisionExecutionResult.executed("create_quest", createdQuest));

        VisionExecutionResult result = executionService.execute(conversation);

        assertTrue(result.isExecuted());
        assertNotNull(result.getCreatedQuest());
        assertEquals(501L, result.getCreatedQuest().getId());
    }

    @Test
    void executesConfirmedCreateCircleConversationThroughAdapter() {
        VisionProperties visionProperties = new VisionProperties();
        visionProperties.setExecutionEnabled(true);

        VisionCreateCircleExecutionAdapter circleAdapter = mock(VisionCreateCircleExecutionAdapter.class);
        when(circleAdapter.capabilityId()).thenReturn("create_circle");
        VisionCreateQuestExecutionAdapter questAdapter = mock(VisionCreateQuestExecutionAdapter.class);
        when(questAdapter.capabilityId()).thenReturn("create_quest");
        VisionUpdateProfileExecutionAdapter profileAdapter = mock(VisionUpdateProfileExecutionAdapter.class);
        when(profileAdapter.capabilityId()).thenReturn("update_profile");
        VisionUpdateProfileLocationExecutionAdapter profileLocationAdapter = mock(VisionUpdateProfileLocationExecutionAdapter.class);
        when(profileLocationAdapter.capabilityId()).thenReturn("update_profile_location");
        VisionCreateCircleRequestExecutionAdapter createCircleRequestAdapter = mock(VisionCreateCircleRequestExecutionAdapter.class);
        when(createCircleRequestAdapter.capabilityId()).thenReturn("create_circle_request");
        VisionAcceptCircleRequestExecutionAdapter acceptCircleRequestAdapter = mock(VisionAcceptCircleRequestExecutionAdapter.class);
        when(acceptCircleRequestAdapter.capabilityId()).thenReturn("accept_circle_request");
        VisionDeleteCircleRequestExecutionAdapter deleteCircleRequestAdapter = mock(VisionDeleteCircleRequestExecutionAdapter.class);
        when(deleteCircleRequestAdapter.capabilityId()).thenReturn("delete_circle_request");
        VisionCreateApplicationExecutionAdapter createApplicationAdapter = mock(VisionCreateApplicationExecutionAdapter.class);
        when(createApplicationAdapter.capabilityId()).thenReturn("create_application");
        VisionUpdateApplicationExecutionAdapter updateApplicationAdapter = mock(VisionUpdateApplicationExecutionAdapter.class);
        when(updateApplicationAdapter.capabilityId()).thenReturn("update_application");
        VisionWithdrawApplicationExecutionAdapter withdrawApplicationAdapter = mock(VisionWithdrawApplicationExecutionAdapter.class);
        when(withdrawApplicationAdapter.capabilityId()).thenReturn("withdraw_application");
        VisionApproveApplicationExecutionAdapter approveApplicationAdapter = mock(VisionApproveApplicationExecutionAdapter.class);
        when(approveApplicationAdapter.capabilityId()).thenReturn("approve_application");
        VisionDeclineApplicationExecutionAdapter declineApplicationAdapter = mock(VisionDeclineApplicationExecutionAdapter.class);
        when(declineApplicationAdapter.capabilityId()).thenReturn("decline_application");
        VisionUpdateCircleExecutionAdapter updateCircleAdapter = mock(VisionUpdateCircleExecutionAdapter.class);
        when(updateCircleAdapter.capabilityId()).thenReturn("update_circle");
        VisionDeleteCircleExecutionAdapter deleteCircleAdapter = mock(VisionDeleteCircleExecutionAdapter.class);
        when(deleteCircleAdapter.capabilityId()).thenReturn("delete_circle");
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(
                        questAdapter,
                        circleAdapter,
                        createCircleRequestAdapter,
                        acceptCircleRequestAdapter,
                        deleteCircleRequestAdapter,
                        createApplicationAdapter,
                        updateApplicationAdapter,
                        withdrawApplicationAdapter,
                        approveApplicationAdapter,
                        declineApplicationAdapter,
                        updateCircleAdapter,
                        deleteCircleAdapter,
                        profileAdapter,
                        profileLocationAdapter
                )
        );

        AppUser user = TestFixtures.user(7L, "vision-user");
        CircleGroupResponseDTO createdCircle = CircleGroupResponseDTO.builder()
                .id(901L)
                .name("Neighbours")
                .memberCount(0)
                .build();

        VisionConversationTestBuilder builder = VisionConversationTestBuilder.createCircle(3L, user)
                .status(VisionConversationStatus.REVIEW_READY)
                .slot("circle_name", "Neighbours");
        var conversation = builder.build();
        when(circleAdapter.execute(conversation)).thenReturn(VisionExecutionResult.executedCircle("create_circle", createdCircle));

        VisionExecutionResult result = executionService.execute(conversation);

        assertTrue(result.isExecuted());
        assertNotNull(result.getCreatedCircle());
        assertEquals(901L, result.getCreatedCircle().getId());
    }

    @Test
    void executesConfirmedUpdateProfileConversationThroughAdapter() {
        VisionProperties visionProperties = new VisionProperties();
        visionProperties.setExecutionEnabled(true);

        VisionCreateQuestExecutionAdapter questAdapter = mock(VisionCreateQuestExecutionAdapter.class);
        when(questAdapter.capabilityId()).thenReturn("create_quest");
        VisionCreateCircleExecutionAdapter circleAdapter = mock(VisionCreateCircleExecutionAdapter.class);
        when(circleAdapter.capabilityId()).thenReturn("create_circle");
        VisionUpdateProfileExecutionAdapter profileAdapter = mock(VisionUpdateProfileExecutionAdapter.class);
        when(profileAdapter.capabilityId()).thenReturn("update_profile");
        VisionUpdateProfileLocationExecutionAdapter profileLocationAdapter = mock(VisionUpdateProfileLocationExecutionAdapter.class);
        when(profileLocationAdapter.capabilityId()).thenReturn("update_profile_location");
        VisionCreateCircleRequestExecutionAdapter createCircleRequestAdapter = mock(VisionCreateCircleRequestExecutionAdapter.class);
        when(createCircleRequestAdapter.capabilityId()).thenReturn("create_circle_request");
        VisionAcceptCircleRequestExecutionAdapter acceptCircleRequestAdapter = mock(VisionAcceptCircleRequestExecutionAdapter.class);
        when(acceptCircleRequestAdapter.capabilityId()).thenReturn("accept_circle_request");
        VisionDeleteCircleRequestExecutionAdapter deleteCircleRequestAdapter = mock(VisionDeleteCircleRequestExecutionAdapter.class);
        when(deleteCircleRequestAdapter.capabilityId()).thenReturn("delete_circle_request");
        VisionCreateApplicationExecutionAdapter createApplicationAdapter = mock(VisionCreateApplicationExecutionAdapter.class);
        when(createApplicationAdapter.capabilityId()).thenReturn("create_application");
        VisionUpdateApplicationExecutionAdapter updateApplicationAdapter = mock(VisionUpdateApplicationExecutionAdapter.class);
        when(updateApplicationAdapter.capabilityId()).thenReturn("update_application");
        VisionWithdrawApplicationExecutionAdapter withdrawApplicationAdapter = mock(VisionWithdrawApplicationExecutionAdapter.class);
        when(withdrawApplicationAdapter.capabilityId()).thenReturn("withdraw_application");
        VisionApproveApplicationExecutionAdapter approveApplicationAdapter = mock(VisionApproveApplicationExecutionAdapter.class);
        when(approveApplicationAdapter.capabilityId()).thenReturn("approve_application");
        VisionDeclineApplicationExecutionAdapter declineApplicationAdapter = mock(VisionDeclineApplicationExecutionAdapter.class);
        when(declineApplicationAdapter.capabilityId()).thenReturn("decline_application");
        VisionUpdateCircleExecutionAdapter updateCircleAdapter = mock(VisionUpdateCircleExecutionAdapter.class);
        when(updateCircleAdapter.capabilityId()).thenReturn("update_circle");
        VisionDeleteCircleExecutionAdapter deleteCircleAdapter = mock(VisionDeleteCircleExecutionAdapter.class);
        when(deleteCircleAdapter.capabilityId()).thenReturn("delete_circle");
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(
                        questAdapter,
                        circleAdapter,
                        createCircleRequestAdapter,
                        acceptCircleRequestAdapter,
                        deleteCircleRequestAdapter,
                        createApplicationAdapter,
                        updateApplicationAdapter,
                        withdrawApplicationAdapter,
                        approveApplicationAdapter,
                        declineApplicationAdapter,
                        updateCircleAdapter,
                        deleteCircleAdapter,
                        profileAdapter,
                        profileLocationAdapter
                )
        );

        AppUser user = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = new VisionConversation();
        conversation.setId(4L);
        conversation.setOwner(user);
        conversation.setIntent(VisionIntent.UPDATE_PROFILE);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        conversation.setSlotData(new java.util.LinkedHashMap<>());
        conversation.getSlotData().put("profile_username", "vision-josip");
        conversation.getSlotData().put("profile_description", "Reliable mover");
        AppUserResponseDTO updatedProfile = AppUserResponseDTO.builder()
                .id(user.getId())
                .username("vision-josip")
                .email("vision-user@example.com")
                .profileDescription("Reliable mover")
                .build();
        when(profileAdapter.execute(conversation)).thenReturn(VisionExecutionResult.executedProfile("update_profile", updatedProfile));

        VisionExecutionResult result = executionService.execute(conversation);

        assertTrue(result.isExecuted());
        assertNotNull(result.getUpdatedProfile());
        assertEquals("vision-josip", result.getUpdatedProfile().getUsername());
    }

    @Test
    void executesConfirmedCreateCircleRequestConversationThroughAdapter() {
        VisionProperties visionProperties = new VisionProperties();
        visionProperties.setExecutionEnabled(true);

        VisionCreateQuestExecutionAdapter questAdapter = mock(VisionCreateQuestExecutionAdapter.class);
        when(questAdapter.capabilityId()).thenReturn("create_quest");
        VisionCreateCircleExecutionAdapter circleAdapter = mock(VisionCreateCircleExecutionAdapter.class);
        when(circleAdapter.capabilityId()).thenReturn("create_circle");
        VisionCreateCircleRequestExecutionAdapter createCircleRequestAdapter = mock(VisionCreateCircleRequestExecutionAdapter.class);
        when(createCircleRequestAdapter.capabilityId()).thenReturn("create_circle_request");
        VisionCreateApplicationExecutionAdapter createApplicationAdapter = mock(VisionCreateApplicationExecutionAdapter.class);
        when(createApplicationAdapter.capabilityId()).thenReturn("create_application");
        VisionUpdateApplicationExecutionAdapter updateApplicationAdapter = mock(VisionUpdateApplicationExecutionAdapter.class);
        when(updateApplicationAdapter.capabilityId()).thenReturn("update_application");
        VisionWithdrawApplicationExecutionAdapter withdrawApplicationAdapter = mock(VisionWithdrawApplicationExecutionAdapter.class);
        when(withdrawApplicationAdapter.capabilityId()).thenReturn("withdraw_application");
        VisionApproveApplicationExecutionAdapter approveApplicationAdapter = mock(VisionApproveApplicationExecutionAdapter.class);
        when(approveApplicationAdapter.capabilityId()).thenReturn("approve_application");
        VisionDeclineApplicationExecutionAdapter declineApplicationAdapter = mock(VisionDeclineApplicationExecutionAdapter.class);
        when(declineApplicationAdapter.capabilityId()).thenReturn("decline_application");
        VisionAcceptCircleRequestExecutionAdapter acceptCircleRequestAdapter = mock(VisionAcceptCircleRequestExecutionAdapter.class);
        when(acceptCircleRequestAdapter.capabilityId()).thenReturn("accept_circle_request");
        VisionDeleteCircleRequestExecutionAdapter deleteCircleRequestAdapter = mock(VisionDeleteCircleRequestExecutionAdapter.class);
        when(deleteCircleRequestAdapter.capabilityId()).thenReturn("delete_circle_request");
        VisionUpdateProfileExecutionAdapter profileAdapter = mock(VisionUpdateProfileExecutionAdapter.class);
        when(profileAdapter.capabilityId()).thenReturn("update_profile");
        VisionUpdateProfileLocationExecutionAdapter profileLocationAdapter = mock(VisionUpdateProfileLocationExecutionAdapter.class);
        when(profileLocationAdapter.capabilityId()).thenReturn("update_profile_location");
        VisionUpdateCircleExecutionAdapter updateCircleAdapter = mock(VisionUpdateCircleExecutionAdapter.class);
        when(updateCircleAdapter.capabilityId()).thenReturn("update_circle");
        VisionDeleteCircleExecutionAdapter deleteCircleAdapter = mock(VisionDeleteCircleExecutionAdapter.class);
        when(deleteCircleAdapter.capabilityId()).thenReturn("delete_circle");
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(
                        questAdapter,
                        circleAdapter,
                        createCircleRequestAdapter,
                        createApplicationAdapter,
                        updateApplicationAdapter,
                        withdrawApplicationAdapter,
                        approveApplicationAdapter,
                        declineApplicationAdapter,
                        acceptCircleRequestAdapter,
                        deleteCircleRequestAdapter,
                        updateCircleAdapter,
                        deleteCircleAdapter,
                        profileAdapter,
                        profileLocationAdapter
                )
        );

        AppUser user = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = new VisionConversation();
        conversation.setId(5L);
        conversation.setOwner(user);
        conversation.setIntent(VisionIntent.CREATE_CIRCLE_REQUEST);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        conversation.setSlotData(new java.util.LinkedHashMap<>());
        conversation.getSlotData().put("circle_request_target_user_id", "12");
        CircleRequestResponseDTO circleRequest = CircleRequestResponseDTO.builder()
                .id(88L)
                .requesterId(user.getId())
                .requesterUsername(user.getUsername())
                .recipientId(12L)
                .recipientUsername("josip")
                .counterpartUserId(12L)
                .counterpartUsername("josip")
                .requestSummaryLabel("Circle request")
                .createdAt(java.time.Instant.now())
                .exactResolutionEligible(true)
                .build();
        when(createCircleRequestAdapter.execute(conversation)).thenReturn(
                VisionExecutionResult.executedCircleRequest("create_circle_request", circleRequest)
        );

        VisionExecutionResult result = executionService.execute(conversation);

        assertTrue(result.isExecuted());
        assertNotNull(result.getCircleRequest());
        assertEquals(88L, result.getCircleRequest().getId());
    }

    @Test
    void executesConfirmedCreateApplicationConversationThroughAdapter() {
        VisionProperties visionProperties = new VisionProperties();
        visionProperties.setExecutionEnabled(true);

        VisionCreateQuestExecutionAdapter questAdapter = mock(VisionCreateQuestExecutionAdapter.class);
        when(questAdapter.capabilityId()).thenReturn("create_quest");
        VisionCreateCircleExecutionAdapter circleAdapter = mock(VisionCreateCircleExecutionAdapter.class);
        when(circleAdapter.capabilityId()).thenReturn("create_circle");
        VisionCreateCircleRequestExecutionAdapter createCircleRequestAdapter = mock(VisionCreateCircleRequestExecutionAdapter.class);
        when(createCircleRequestAdapter.capabilityId()).thenReturn("create_circle_request");
        VisionCreateApplicationExecutionAdapter createApplicationAdapter = mock(VisionCreateApplicationExecutionAdapter.class);
        when(createApplicationAdapter.capabilityId()).thenReturn("create_application");
        VisionUpdateApplicationExecutionAdapter updateApplicationAdapter = mock(VisionUpdateApplicationExecutionAdapter.class);
        when(updateApplicationAdapter.capabilityId()).thenReturn("update_application");
        VisionWithdrawApplicationExecutionAdapter withdrawApplicationAdapter = mock(VisionWithdrawApplicationExecutionAdapter.class);
        when(withdrawApplicationAdapter.capabilityId()).thenReturn("withdraw_application");
        VisionApproveApplicationExecutionAdapter approveApplicationAdapter = mock(VisionApproveApplicationExecutionAdapter.class);
        when(approveApplicationAdapter.capabilityId()).thenReturn("approve_application");
        VisionDeclineApplicationExecutionAdapter declineApplicationAdapter = mock(VisionDeclineApplicationExecutionAdapter.class);
        when(declineApplicationAdapter.capabilityId()).thenReturn("decline_application");
        VisionAcceptCircleRequestExecutionAdapter acceptCircleRequestAdapter = mock(VisionAcceptCircleRequestExecutionAdapter.class);
        when(acceptCircleRequestAdapter.capabilityId()).thenReturn("accept_circle_request");
        VisionDeleteCircleRequestExecutionAdapter deleteCircleRequestAdapter = mock(VisionDeleteCircleRequestExecutionAdapter.class);
        when(deleteCircleRequestAdapter.capabilityId()).thenReturn("delete_circle_request");
        VisionUpdateCircleExecutionAdapter updateCircleAdapter = mock(VisionUpdateCircleExecutionAdapter.class);
        when(updateCircleAdapter.capabilityId()).thenReturn("update_circle");
        VisionDeleteCircleExecutionAdapter deleteCircleAdapter = mock(VisionDeleteCircleExecutionAdapter.class);
        when(deleteCircleAdapter.capabilityId()).thenReturn("delete_circle");
        VisionUpdateProfileExecutionAdapter profileAdapter = mock(VisionUpdateProfileExecutionAdapter.class);
        when(profileAdapter.capabilityId()).thenReturn("update_profile");
        VisionUpdateProfileLocationExecutionAdapter profileLocationAdapter = mock(VisionUpdateProfileLocationExecutionAdapter.class);
        when(profileLocationAdapter.capabilityId()).thenReturn("update_profile_location");
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(
                        questAdapter,
                        circleAdapter,
                        createCircleRequestAdapter,
                        createApplicationAdapter,
                        updateApplicationAdapter,
                        withdrawApplicationAdapter,
                        approveApplicationAdapter,
                        declineApplicationAdapter,
                        acceptCircleRequestAdapter,
                        deleteCircleRequestAdapter,
                        updateCircleAdapter,
                        deleteCircleAdapter,
                        profileAdapter,
                        profileLocationAdapter
                )
        );

        AppUser user = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = new VisionConversation();
        conversation.setId(6L);
        conversation.setOwner(user);
        conversation.setIntent(VisionIntent.CREATE_APPLICATION);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        conversation.setSlotData(new java.util.LinkedHashMap<>());
        conversation.getSlotData().put("application_quest_id", "55");
        conversation.getSlotData().put("application_message", "I can do it tomorrow");
        conversation.getSlotData().put("application_proposed_price", "20");
        QuestApplicationResponseDTO application = QuestApplicationResponseDTO.builder()
                .id(91L)
                .questId(55L)
                .message("I can do it tomorrow")
                .build();
        when(createApplicationAdapter.execute(conversation)).thenReturn(
                VisionExecutionResult.executedApplication("create_application", application)
        );

        VisionExecutionResult result = executionService.execute(conversation);

        assertTrue(result.isExecuted());
        assertNotNull(result.getApplication());
        assertEquals(91L, result.getApplication().getId());
    }

    @Test
    void rejectsUnsupportedExecutionAdaptersAtRegistrationTime() {
        VisionProperties visionProperties = new VisionProperties();
        VisionCapabilityExecutionAdapter unsupportedAdapter = mock(VisionCapabilityExecutionAdapter.class);
        when(unsupportedAdapter.capabilityId()).thenReturn("view_profile");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                new VisionExecutionService(
                        new VisionSemanticRouteCatalogService(),
                        new VisionSurfacePolicy(visionProperties),
                        List.of(unsupportedAdapter)
                ));

        assertEquals("Unsupported Vision execution adapter registered for capability view_profile", exception.getMessage());
    }
}
