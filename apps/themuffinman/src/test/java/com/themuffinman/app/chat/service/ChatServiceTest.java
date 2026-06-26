package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatMessageRequestDTO;
import com.themuffinman.app.chat.dto.ChatOpenConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.model.ChatMessage;
import com.themuffinman.app.chat.model.ChatPresence;
import com.themuffinman.app.chat.repository.ChatConversationRepository;
import com.themuffinman.app.chat.repository.ChatMessageRepository;
import com.themuffinman.app.chat.repository.ChatPresenceRepository;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.social.service.CircleRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatConversationRepository chatConversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatPresenceRepository chatPresenceRepository;

    @Mock
    private AppUserLookupService appUserLookupService;

    @Mock
    private CircleRelationService circleRelationService;

    @Mock
    private CircleMembershipService circleMembershipService;

    @Mock
    private ChatPresenceService chatPresenceService;

    @Mock
    private ChatRealtimeService chatRealtimeService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void openConversationCreatesConversationForCircleContact() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatOpenConversationRequestDTO request = ChatOpenConversationRequestDTO.builder()
                .otherUserId(otherUser.getId())
                .build();

        when(appUserLookupService.requireById(otherUser.getId())).thenReturn(otherUser);
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatConversationRepository.findByLeftParticipantIdAndRightParticipantId(1L, 2L)).thenReturn(Optional.empty());
        when(chatConversationRepository.save(any(ChatConversation.class))).thenAnswer(invocation -> {
            ChatConversation conversation = invocation.getArgument(0);
            conversation.setId(7L);
            return conversation;
        });
        when(chatPresenceRepository.findByUserId(otherUser.getId())).thenReturn(Optional.empty());
        when(chatPresenceService.isOnline(any(), any())).thenReturn(false);

        var result = chatService.openConversation(request, currentUser);

        assertEquals(7L, result.getConversationId());
        assertEquals(otherUser.getId(), result.getOtherUserId());
        verify(chatConversationRepository).save(any(ChatConversation.class));
    }

    @Test
    void openConversationRejectsUserOutsideCircles() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatOpenConversationRequestDTO request = ChatOpenConversationRequestDTO.builder()
                .otherUserId(otherUser.getId())
                .build();

        when(appUserLookupService.requireById(otherUser.getId())).thenReturn(otherUser);
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> chatService.openConversation(request, currentUser));
    }

    @Test
    void sendMessageUpdatesConversationPreview() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        ChatMessageRequestDTO request = ChatMessageRequestDTO.builder()
                .messageBody("Hello there from the chat")
                .build();

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(8L);
            return message;
        });

        var result = chatService.sendMessage(5L, request, currentUser);

        assertEquals(8L, result.getId());
        assertEquals("Hello there from the chat", conversation.getLastMessagePreview());
        assertFalse(conversation.isLastMessageHasImage());
        verify(chatConversationRepository).save(conversation);
        verify(chatRealtimeService).notifyConversationChanged(conversation, currentUser.getId(), "message_created");
    }

    @Test
    void markConversationReadMarksIncomingMessages() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        ChatMessage unreadMessage = new ChatMessage();
        unreadMessage.setConversation(conversation);
        unreadMessage.setSender(otherUser);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatMessageRepository.findUnreadIncomingByConversationId(5L, 1L)).thenReturn(List.of(unreadMessage));

        chatService.markConversationRead(5L, currentUser);

        assertEquals(false, unreadMessage.getReadAt() == null);
        verify(chatMessageRepository).saveAll(List.of(unreadMessage));
    }

    @Test
    void getWorkspaceBuildsContactsAndUnreadConversationCounts() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        conversation.setLastMessageAt(Instant.now());
        conversation.setLastMessagePreview("Need help");
        conversation.setLastMessageSender(otherUser);

        CircleGroup family = new CircleGroup();
        family.setId(11L);
        family.setName("Family");

        CircleMembership membership = new CircleMembership();
        membership.setCircle(family);
        membership.setMember(otherUser);

        ChatPresence presence = new ChatPresence();
        presence.setUser(otherUser);
        presence.setLastActiveAt(Instant.now());

        ChatMessageRepository.UnreadCountRow unreadCountRow = new ChatMessageRepository.UnreadCountRow() {
            @Override
            public Long getConversationId() {
                return 5L;
            }

            @Override
            public long getUnreadCount() {
                return 2L;
            }
        };

        when(chatConversationRepository.findDetailedByParticipantId(currentUser.getId())).thenReturn(List.of(conversation));
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), currentUser.getId())).thenReturn(List.of(unreadCountRow));
        when(circleMembershipService.getMembershipsByUserIdForOwner(currentUser.getId())).thenReturn(Map.of(otherUser.getId(), List.of(membership)));
        when(chatPresenceRepository.findByUserIds(List.of(otherUser.getId()))).thenReturn(List.of(presence));
        when(chatPresenceService.isOnline(any(), any())).thenReturn(true);

        ChatWorkspaceDTO result = chatService.getWorkspace(currentUser);

        assertEquals(1, result.getContacts().size());
        assertEquals(1, result.getConversations().size());
        assertEquals(1, result.getUnreadConversationCount());
        assertEquals(1, result.getOnlineContactCount());
        assertEquals("Family", result.getCircles().getFirst().getName());
    }

    @Test
    void heartbeatCreatesOrUpdatesPresenceRow() {
        AppUser currentUser = createUser(1L, "mia");

        chatService.heartbeat(currentUser);

        verify(chatPresenceService).markActive(currentUser);
    }

    private AppUser createUser(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@sidequest.test");
        user.setPasswordHash("hash");
        return user;
    }

    private ChatConversation createConversation(Long id, AppUser leftParticipant, AppUser rightParticipant) {
        ChatConversation conversation = new ChatConversation();
        conversation.setId(id);
        conversation.setLeftParticipant(leftParticipant);
        conversation.setRightParticipant(rightParticipant);
        return conversation;
    }
}
