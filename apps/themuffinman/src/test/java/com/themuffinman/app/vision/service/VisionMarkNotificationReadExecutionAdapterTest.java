package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisionMarkNotificationReadExecutionAdapterTest {

    @Test
    void marksOneNotificationThroughTheBackendService() {
        WorkmarketQuestNewsService newsService = mock(WorkmarketQuestNewsService.class);
        VisionMarkNotificationReadExecutionAdapter adapter = new VisionMarkNotificationReadExecutionAdapter(newsService);
        AppUser owner = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);
        conversation.setSlotData(new LinkedHashMap<>(Map.of("notification_id", "42")));

        VisionExecutionResult result = adapter.execute(conversation);

        assertTrue(result.isExecuted());
        verify(newsService).markMyNewsItemAsRead(42L, owner);
    }
}
