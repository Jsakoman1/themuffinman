package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.service.WorkmarketQuestUpdateService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisionCancelQuestExecutionAdapterTest {

    @Test
    void cancelsQuestThroughTheBackendUpdateService() {
        WorkmarketQuestUpdateService updateService = mock(WorkmarketQuestUpdateService.class);
        VisionCancelQuestExecutionAdapter adapter = new VisionCancelQuestExecutionAdapter(updateService);
        AppUser owner = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);
        conversation.setSlotData(new LinkedHashMap<>(Map.of("quest_id", "42")));
        when(updateService.cancelQuestForVision(42L, owner)).thenReturn(new Quest());

        VisionExecutionResult result = adapter.execute(conversation);

        assertTrue(result.isExecuted());
        verify(updateService).cancelQuestForVision(42L, owner);
    }
}
