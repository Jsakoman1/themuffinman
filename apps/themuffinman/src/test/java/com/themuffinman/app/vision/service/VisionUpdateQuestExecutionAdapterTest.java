package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.service.WorkmarketQuestUpdateService;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisionUpdateQuestExecutionAdapterTest {
    @Test
    void updatesOwnedQuestTitleThroughBackendService() {
        WorkmarketQuestUpdateService service = mock(WorkmarketQuestUpdateService.class);
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(new AppUser());
        conversation.setSlotData(new LinkedHashMap<>(Map.of("quest_id", "42", "quest_title", "Move sofa")));
        assertTrue(new VisionUpdateQuestExecutionAdapter(service).execute(conversation).isExecuted());
        verify(service).updateQuestTitleForVision(42L, "Move sofa", conversation.getOwner());
    }
}
