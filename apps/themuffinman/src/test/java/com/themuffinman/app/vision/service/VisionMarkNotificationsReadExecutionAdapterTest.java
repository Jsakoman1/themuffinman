package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisionMarkNotificationsReadExecutionAdapterTest {

    @Test
    void marksAllNotificationsThroughTheBackendService() {
        WorkmarketQuestNewsService newsService = mock(WorkmarketQuestNewsService.class);
        VisionMarkNotificationsReadExecutionAdapter adapter = new VisionMarkNotificationsReadExecutionAdapter(newsService);
        AppUser owner = new AppUser();
        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(owner);

        VisionExecutionResult result = adapter.execute(conversation);

        assertTrue(result.isExecuted());
        verify(newsService).markMyNewsAsRead(owner);
    }
}
