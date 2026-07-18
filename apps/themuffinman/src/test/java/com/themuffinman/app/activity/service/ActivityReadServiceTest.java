package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.repository.ActivityResumeDismissalRepository;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActivityReadServiceTest {
    @Test void rejectsUnboundedWorkspaceActivityLimit() {
        WorkmarketQuestNewsService news = mock(WorkmarketQuestNewsService.class);
        AppUser user = new AppUser();
        when(news.getMyNews(user)).thenReturn(List.of());
        ActivityReadService service = new ActivityReadService(news, mock(WorkmarketQuestNewsMgr.class), mock(VisionConversationRepository.class), mock(ActivityResumeDismissalRepository.class));

        assertThrows(IllegalArgumentException.class, () -> service.getWorkspaceActivity(user, 0));
        assertThrows(IllegalArgumentException.class, () -> service.getWorkspaceActivity(user, 51));
    }
}
