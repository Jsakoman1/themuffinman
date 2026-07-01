package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import com.themuffinman.app.vision.testing.VisionSlotStatePresets;
import com.themuffinman.app.vision.model.Quest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionExecutionServiceTest {

    @Test
    void blocksExecutionWhenFeatureFlagIsDisabled() {
        VisionProperties visionProperties = new VisionProperties();
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSurfacePolicy(visionProperties),
                mock(VisionCreateQuestExecutionAdapter.class)
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
        VisionExecutionService executionService = new VisionExecutionService(
                new VisionSurfacePolicy(visionProperties),
                adapter
        );

        AppUser user = TestFixtures.user(7L, "vision-user");
        Quest createdQuest = new Quest();
        createdQuest.setId(501L);

        VisionConversationTestBuilder builder = VisionConversationTestBuilder.createQuest(2L, user)
                .status(VisionConversationStatus.REVIEW_READY)
                .slots(VisionSlotStatePresets.createQuestReviewReadyProfileLocation());
        var conversation = builder.build();
        when(adapter.execute(conversation.getSlotData(), user)).thenReturn(createdQuest);

        VisionExecutionResult result = executionService.execute(conversation);

        assertTrue(result.isExecuted());
        assertNotNull(result.getCreatedQuest());
        assertEquals(501L, result.getCreatedQuest().getId());
    }
}
