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

class VisionReopenQuestExecutionAdapterTest {

    @Test
    void reopensQuestThroughTheBackendUpdateService() {
        WorkmarketQuestUpdateService updateService = mock(WorkmarketQuestUpdateService.class);
        VisionReopenQuestExecutionAdapter adapter = new VisionReopenQuestExecutionAdapter(updateService);
        AppUser owner = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);
        conversation.setSlotData(new LinkedHashMap<>(Map.of("quest_id", "42")));
        when(updateService.reopenQuestForVision(42L, owner)).thenReturn(new Quest());

        VisionExecutionResult result = adapter.execute(conversation);

        assertTrue(result.isExecuted());
        verify(updateService).reopenQuestForVision(42L, owner);
    }
}
