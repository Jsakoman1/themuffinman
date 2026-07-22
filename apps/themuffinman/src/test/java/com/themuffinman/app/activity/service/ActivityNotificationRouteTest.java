package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.repository.ActivityResumeDismissalRepository;
import com.themuffinman.app.chat.repository.ChatConversationRepository;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.repository.ThingBorrowRequestRepository;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.workmarket.dto.QuestNewsDestinationTypeDTO;
import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ActivityNotificationRouteTest {

    @Test
    void activityUsesBackendApplicationDestinationInsteadOfQuestId() {
        WorkmarketQuestNewsService newsService = mock(WorkmarketQuestNewsService.class);
        WorkmarketQuestNewsMgr mapper = mock(WorkmarketQuestNewsMgr.class);
        ActivityReadService service = new ActivityReadService(
                newsService,
                mapper,
                mock(VisionConversationRepository.class),
                mock(ActivityResumeDismissalRepository.class),
                mock(ChatConversationRepository.class),
                mock(ThingBorrowRequestRepository.class)
        );
        VisionConversationRepository conversations = mock(VisionConversationRepository.class);
        ActivityResumeDismissalRepository dismissals = mock(ActivityResumeDismissalRepository.class);
        ChatConversationRepository chats = mock(ChatConversationRepository.class);
        ThingBorrowRequestRepository requests = mock(ThingBorrowRequestRepository.class);
        service = new ActivityReadService(newsService, mapper, conversations, dismissals, chats, requests);
        AppUser user = new AppUser();
        user.setId(7L);
        QuestNewsItemResponseDTO item = QuestNewsItemResponseDTO.builder()
                .id(1L)
                .title("New application")
                .message("Application update")
                .questId(415L)
                .applicationId(277L)
                .destinationType(QuestNewsDestinationTypeDTO.APPLICATION)
                .destinationId(277L)
                .createdAt(Instant.now())
                .build();
        when(newsService.getMyNews(user)).thenReturn(List.of(mock(com.themuffinman.app.workmarket.model.QuestNewsItem.class)));
        when(mapper.toDto(any())).thenReturn(item);
        when(conversations.findTop5ByOwnerOrderByUpdatedAtDesc(user)).thenReturn(List.of());
        when(chats.findDetailedByParticipantId(7L)).thenReturn(List.of());
        when(requests.findForOwnerDashboard(7L)).thenReturn(List.of());
        when(requests.findForBorrowerDashboard(7L)).thenReturn(List.of());

        assertEquals("/work/applications/277", service.getMine(user).getFirst().getRoute());
    }
}
