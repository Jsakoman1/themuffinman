package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.repository.ActivityResumeDismissalRepository;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.repository.ChatConversationRepository;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.model.ThingBorrowRequest;
import com.themuffinman.app.things.model.ThingListing;
import com.themuffinman.app.things.repository.ThingBorrowRequestRepository;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.web.server.ResponseStatusException;

class ActivityReadServiceTest {
    @Test void rejectsUnboundedWorkspaceActivityLimit() {
        WorkmarketQuestNewsService news = mock(WorkmarketQuestNewsService.class);
        AppUser user = new AppUser();
        when(news.getMyNews(user)).thenReturn(List.of());
        ActivityReadService service = new ActivityReadService(news, mock(WorkmarketQuestNewsMgr.class), mock(VisionConversationRepository.class), mock(ActivityResumeDismissalRepository.class), mock(ChatConversationRepository.class), mock(ThingBorrowRequestRepository.class));

        assertThrows(IllegalArgumentException.class, () -> service.getWorkspaceActivity(user, 0));
        assertThrows(IllegalArgumentException.class, () -> service.getWorkspaceActivity(user, 51));
    }

    @Test
    void rejectsInvalidDismissKeyWithClientError() {
        ActivityReadService service = new ActivityReadService(
                mock(WorkmarketQuestNewsService.class),
                mock(WorkmarketQuestNewsMgr.class),
                mock(VisionConversationRepository.class),
                mock(ActivityResumeDismissalRepository.class),
                mock(ChatConversationRepository.class),
                mock(ThingBorrowRequestRepository.class)
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.dismiss(" ", new AppUser()));

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void projectsAuthorizedChatConversationIntoResumeActivity() {
        AppUser user = new AppUser();
        user.setId(7L);
        ChatConversation conversation = new ChatConversation();
        conversation.setId(41L);
        conversation.setLastMessageAt(java.time.Instant.parse("2026-07-19T10:00:00Z"));
        conversation.setLastMessagePreview("Can you review this?");
        ChatConversationRepository chatRepository = mock(ChatConversationRepository.class);
        when(chatRepository.findDetailedByParticipantId(7L)).thenReturn(List.of(conversation));

        ActivityReadService service = new ActivityReadService(
                mock(WorkmarketQuestNewsService.class),
                mock(WorkmarketQuestNewsMgr.class),
                mock(VisionConversationRepository.class),
                mock(ActivityResumeDismissalRepository.class),
                chatRepository,
                mock(ThingBorrowRequestRepository.class)
        );

        var result = service.getWorkspaceActivity(user, 20);

        assertThat(result).anySatisfy(item -> {
            assertThat(item.getSource()).isEqualTo("chat");
            assertThat(item.getRoute()).isEqualTo("/chat/41");
            assertThat(item.getResumeKey()).isEqualTo("chat:41");
        });
    }

    @Test
    void excludesDismissedChatResumeActivityForTheCurrentViewer() {
        AppUser user = new AppUser();
        user.setId(7L);
        ChatConversation conversation = new ChatConversation();
        conversation.setId(41L);
        conversation.setLastMessageAt(java.time.Instant.parse("2026-07-19T10:00:00Z"));
        ChatConversationRepository chatRepository = mock(ChatConversationRepository.class);
        ActivityResumeDismissalRepository dismissalRepository = mock(ActivityResumeDismissalRepository.class);
        when(chatRepository.findDetailedByParticipantId(7L)).thenReturn(List.of(conversation));
        when(dismissalRepository.existsByUserIdAndResumeKey(7L, "chat:41")).thenReturn(true);

        ActivityReadService service = new ActivityReadService(
                mock(WorkmarketQuestNewsService.class),
                mock(WorkmarketQuestNewsMgr.class),
                mock(VisionConversationRepository.class),
                dismissalRepository,
                chatRepository,
                mock(ThingBorrowRequestRepository.class)
        );

        assertThat(service.getWorkspaceActivity(user, 20))
                .noneMatch(item -> "chat:41".equals(item.getResumeKey()));
    }

    @Test
    void projectsThingBorrowRequestForOwnerIntoViewerScopedActivity() {
        AppUser user = new AppUser(); user.setId(7L);
        AppUser borrower = new AppUser(); borrower.setId(8L); borrower.setUsername("borrower");
        ThingListing listing = new ThingListing(); listing.setId(12L); listing.setTitle("Projector"); listing.setOwner(user);
        ThingBorrowRequest request = new ThingBorrowRequest(); request.setId(31L); request.setListing(listing); request.setBorrower(borrower);
        ThingBorrowRequestRepository things = mock(ThingBorrowRequestRepository.class);
        when(things.findForOwnerDashboard(7L)).thenReturn(List.of(request));
        when(things.findForBorrowerDashboard(7L)).thenReturn(List.of());

        ActivityReadService service = new ActivityReadService(mock(WorkmarketQuestNewsService.class), mock(WorkmarketQuestNewsMgr.class), mock(VisionConversationRepository.class), mock(ActivityResumeDismissalRepository.class), mock(ChatConversationRepository.class), things);

        assertThat(service.getWorkspaceActivity(user, 20)).anySatisfy(item -> {
            assertThat(item.getSource()).isEqualTo("things");
            assertThat(item.getRoute()).isEqualTo("/things/mine");
            assertThat(item.getResumeKey()).isEqualTo("things:request:31:owner");
        });
    }
}
