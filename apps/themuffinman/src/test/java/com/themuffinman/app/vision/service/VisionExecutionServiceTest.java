package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import com.themuffinman.app.vision.testing.VisionSlotStatePresets;
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
    void blocksExecutionWhenFeatureFlagIsDisabled() {
        VisionProperties visionProperties = new VisionProperties();
        VisionCreateQuestExecutionAdapter questAdapter = mock(VisionCreateQuestExecutionAdapter.class);
        when(questAdapter.capabilityId()).thenReturn("create_quest");
        VisionCreateCircleExecutionAdapter circleAdapter = mock(VisionCreateCircleExecutionAdapter.class);
        when(circleAdapter.capabilityId()).thenReturn("create_circle");
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(questAdapter, circleAdapter)
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
    }

    @Test
    void executesConfirmedCreateQuestConversationThroughAdapter() {
        VisionProperties visionProperties = new VisionProperties();
        visionProperties.setExecutionEnabled(true);

        VisionCreateQuestExecutionAdapter adapter = mock(VisionCreateQuestExecutionAdapter.class);
        when(adapter.capabilityId()).thenReturn("create_quest");
        VisionCreateCircleExecutionAdapter circleAdapter = mock(VisionCreateCircleExecutionAdapter.class);
        when(circleAdapter.capabilityId()).thenReturn("create_circle");
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(adapter, circleAdapter)
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
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSemanticRouteCatalogService(),
                new VisionSurfacePolicy(visionProperties),
                List.of(questAdapter, circleAdapter)
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
