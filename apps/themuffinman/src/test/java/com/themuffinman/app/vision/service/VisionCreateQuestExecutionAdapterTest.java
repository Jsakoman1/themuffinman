package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionCreateQuestExecutionAdapterTest {
    @Test
    void convertsQuestValidationFailureIntoStableNonRetryableMetadata() {
        AppUser user = TestFixtures.user(7L, "vision-user");
        WorkmarketQuestService workmarket = mock(WorkmarketQuestService.class);
        when(workmarket.createQuest(any(QuestRequestDTO.class), eq(user)))
                .thenThrow(ServiceErrors.conflict("Quest title is invalid"));
        VisionCreateQuestExecutionAdapter adapter = new VisionCreateQuestExecutionAdapter(
                workmarket, mock(VisionScheduleParserService.class), mock(EntityManager.class));

        VisionExecutionResult result = adapter.execute(conversation(user));

        assertThat(result.getFailureCode()).isEqualTo("VALIDATION");
        assertThat(result.isRetryable()).isFalse();
        assertThat(result.isExecuted()).isFalse();
    }

    @Test
    void convertsUnexpectedQuestFailureIntoRetryableMetadata() {
        AppUser user = TestFixtures.user(7L, "vision-user");
        WorkmarketQuestService workmarket = mock(WorkmarketQuestService.class);
        when(workmarket.createQuest(any(QuestRequestDTO.class), eq(user)))
                .thenThrow(new IllegalStateException("database unavailable"));
        VisionCreateQuestExecutionAdapter adapter = new VisionCreateQuestExecutionAdapter(
                workmarket, mock(VisionScheduleParserService.class), mock(EntityManager.class));

        VisionExecutionResult result = adapter.execute(conversation(user));

        assertThat(result.getFailureCode()).isEqualTo("EXECUTION_FAILED");
        assertThat(result.isRetryable()).isTrue();
        assertThat(result.isExecuted()).isFalse();
    }

    private VisionConversation conversation(AppUser user) {
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(user);
        conversation.setSlotData(new LinkedHashMap<>(Map.of(
                "quest_title", "Move sofa",
                "quest_description", "Move a sofa",
                "schedule_mode", "flexible",
                "location_mode", "off"
        )));
        return conversation;
    }
}
