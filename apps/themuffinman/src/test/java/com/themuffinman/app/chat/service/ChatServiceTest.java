package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatMarkReadRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationParticipantsRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationListDTO;
import com.themuffinman.app.chat.dto.ChatConversationRoleRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationStateRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationTitleRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageRequestDTO;
import com.themuffinman.app.chat.dto.ChatOpenConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatReceiptRequestDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.chat.model.ChatAttachmentUpload;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.model.ChatConversationMemberState;
import com.themuffinman.app.chat.model.ChatConversationParticipant;
import com.themuffinman.app.chat.model.ChatConversationParticipantRole;
import com.themuffinman.app.chat.model.ChatConversationType;
import com.themuffinman.app.chat.model.ChatMessage;
import com.themuffinman.app.chat.model.ChatMessageReaction;
import com.themuffinman.app.chat.repository.ChatMessageReactionRepository;
import com.themuffinman.app.chat.model.ChatPresence;
import com.themuffinman.app.chat.repository.ChatAttachmentUploadRepository;
import com.themuffinman.app.chat.repository.ChatConversationParticipantRepository;
import com.themuffinman.app.chat.repository.ChatConversationRepository;
import com.themuffinman.app.chat.service.ChatAuditService;
import com.themuffinman.app.chat.repository.ChatMessageRepository;
import com.themuffinman.app.chat.repository.ChatPresenceRepository;
import com.themuffinman.app.config.ChatProperties;
import com.themuffinman.app.config.ObjectStorageProperties;
import com.themuffinman.app.config.RetentionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.social.service.CircleRelationService;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.service.WorkmarketQuestAccessPolicyService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestExecutionPrimitiveService;
import com.themuffinman.app.storage.ObjectStorageAccess;
import com.themuffinman.app.storage.ObjectStorageService;
import com.themuffinman.app.storage.StoredObject;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatConversationRepository chatConversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatMessageReactionRepository chatMessageReactionRepository;

    @Mock
    private ChatAttachmentUploadRepository chatAttachmentUploadRepository;

    @Mock
    private ChatConversationParticipantRepository chatConversationParticipantRepository;

    @Mock
    private ChatPresenceRepository chatPresenceRepository;

    @Mock
    private AppUserLookupService appUserLookupService;

    @Mock
    private CircleRelationService circleRelationService;

    @Mock
    private CircleMembershipService circleMembershipService;

    @Mock
    private CircleGroupRepository circleGroupRepository;

    @Mock
    private WorkmarketQuestExecutionPrimitiveService workmarketQuestExecutionPrimitiveService;

    @Mock
    private WorkmarketQuestApplicationRepository workmarketQuestApplicationRepository;

    @Mock
    private WorkmarketQuestAccessPolicyService workmarketQuestAccessPolicyService;

    @Mock
    private ChatPresenceService chatPresenceService;

    @Mock
    private ChatRealtimeService chatRealtimeService;

    @Mock
    private ChatRateLimitService chatRateLimitService;

    @Mock
    private ChatConversationStateService chatConversationStateService;

    @Mock
    private ChatAuditService chatAuditService;

    @Mock
    private ChatProperties chatProperties;

    @Mock
    private RetentionProperties retentionProperties;

    @Mock
    private ObjectStorageProperties objectStorageProperties;

    @Mock
    private ObjectStorageService objectStorageService;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        ChatProperties.Messages messages = new ChatProperties.Messages();
        ChatProperties.Presence presence = new ChatProperties.Presence();
        ChatProperties.Attachments attachments = new ChatProperties.Attachments();
        ChatProperties.Moderation moderation = new ChatProperties.Moderation();
        ChatProperties.Typing typing = new ChatProperties.Typing();
        ChatProperties.Support support = new ChatProperties.Support();
        lenient().when(chatProperties.getMessages()).thenReturn(messages);
        lenient().when(chatProperties.getPresence()).thenReturn(presence);
        lenient().when(chatProperties.getAttachments()).thenReturn(attachments);
        lenient().when(chatProperties.getModeration()).thenReturn(moderation);
        lenient().when(chatProperties.getTyping()).thenReturn(typing);
        lenient().when(chatProperties.getSupport()).thenReturn(support);
        lenient().when(objectStorageProperties.getKeyPrefix()).thenReturn("chat");
        lenient().when(chatConversationStateService.getStatesByConversationId(any(), any())).thenReturn(Map.of());
        lenient().when(chatConversationStateService.markConversationOpened(any(), any(), any(), anyBoolean())).thenReturn(null);
        lenient().when(chatConversationStateService.isArchived(nullable(ChatConversationMemberState.class)))
                .thenAnswer(invocation -> {
                    ChatConversationMemberState state = invocation.getArgument(0);
                    return state != null && state.getArchivedAt() != null;
                });
        lenient().when(chatConversationStateService.isMuted(nullable(ChatConversationMemberState.class), any()))
                .thenAnswer(invocation -> {
                    ChatConversationMemberState state = invocation.getArgument(0);
                    Instant now = invocation.getArgument(1);
                    return state != null && state.getMutedUntil() != null && state.getMutedUntil().isAfter(now);
                });
    }

    @Test
    void openConversationCreatesConversationForCircleContact() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatOpenConversationRequestDTO request = ChatOpenConversationRequestDTO.builder()
                .otherUserId(otherUser.getId())
                .build();

        when(appUserLookupService.requireById(otherUser.getId())).thenReturn(otherUser);
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatConversationRepository.findByLeftParticipantIdAndRightParticipantIdAndConversationType(1L, 2L, ChatConversationType.DIRECT)).thenReturn(Optional.empty());
        when(chatConversationRepository.save(any(ChatConversation.class))).thenAnswer(invocation -> {
            ChatConversation conversation = invocation.getArgument(0);
            conversation.setId(7L);
            return conversation;
        });
        when(chatPresenceService.isOnline(any(), any())).thenReturn(false);

        var result = chatService.openConversation(request, currentUser);

        assertEquals(7L, result.getConversationId());
        assertEquals(otherUser.getId(), result.getOtherUserId());
        assertEquals("conversation:7", result.getResolutionKey());
        assertEquals("Chat with john", result.getResolutionLabel());
        assertEquals(true, result.isExactResolutionEligible());
        verify(chatConversationRepository, times(2)).save(any(ChatConversation.class));
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
        when(chatMessageRepository.findLatestDetailedByConversationId(eq(5L), any(Pageable.class)))
                .thenReturn(List.of(buildMessage(8L, conversation, currentUser, "Hello there from the chat", null)));
        when(chatPresenceService.isOnline(any(), any())).thenReturn(false);

        var result = chatService.sendMessage(5L, request, currentUser);

        assertEquals(8L, result.getId());
        assertEquals("Hello there from the chat", conversation.getLastMessagePreview());
        assertFalse(conversation.isLastMessageHasImage());
        verify(chatConversationRepository).save(conversation);
        verify(chatRealtimeService).notifyMessageCreated(eq(conversation), eq(currentUser.getId()), any(), any());
    }

    @Test
    void sendMessageConsumesUploadedAttachment() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        ChatAttachmentUpload upload = new ChatAttachmentUpload();
        upload.setId(31L);
        upload.setUploadedBy(currentUser);
        upload.setStorageProvider("s3");
        upload.setStorageKey("chat/1/2026-07-08/object-brief.pdf");
        upload.setAttachmentName("brief.pdf");
        upload.setAttachmentMimeType("application/pdf");
        upload.setAttachmentSizeBytes(5);
        ChatMessageRequestDTO request = ChatMessageRequestDTO.builder()
                .attachmentUploadId(31L)
                .build();

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatAttachmentUploadRepository.findById(31L)).thenReturn(Optional.of(upload));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(8L);
            return message;
        });
        when(chatAttachmentUploadRepository.save(any(ChatAttachmentUpload.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.findLatestDetailedByConversationId(eq(5L), any(Pageable.class)))
                .thenReturn(List.of(buildMessage(8L, conversation, currentUser, null, null)));
        when(chatPresenceService.isOnline(any(), any())).thenReturn(false);
        when(objectStorageService.resolve("chat/1/2026-07-08/object-brief.pdf"))
                .thenReturn(new ObjectStorageAccess("s3", "chat/1/2026-07-08/object-brief.pdf", "https://cdn.example.test/chat/1/2026-07-08/object-brief.pdf", Instant.parse("2026-07-08T10:15:30Z")));

        var result = chatService.sendMessage(5L, request, currentUser);

        assertEquals("brief.pdf", result.getAttachmentName());
        assertEquals("chat/1/2026-07-08/object-brief.pdf", result.getAttachmentStorageKey());
        assertEquals(8L, upload.getConsumedMessageId());
        assertEquals(false, upload.getConsumedAt() == null);
    }

    @Test
    void uploadAttachmentBuildsAttachmentPayload() {
        AppUser currentUser = createUser(1L, "mia");
        MockMultipartFile file = new MockMultipartFile("file", "brief.pdf", "application/pdf", "hello".getBytes());
        when(objectStorageService.store(any(), eq("application/pdf"), any()))
                .thenReturn(new StoredObject("s3", "chat/1/2026-07-08/object-brief.pdf"));
        when(chatAttachmentUploadRepository.save(any(ChatAttachmentUpload.class))).thenAnswer(invocation -> {
            ChatAttachmentUpload upload = invocation.getArgument(0);
            upload.setId(31L);
            return upload;
        });
        when(objectStorageService.resolve("chat/1/2026-07-08/object-brief.pdf"))
                .thenReturn(new ObjectStorageAccess("s3", "chat/1/2026-07-08/object-brief.pdf", "https://cdn.example.test/chat/1/2026-07-08/object-brief.pdf", Instant.parse("2026-07-08T10:15:30Z")));

        var result = chatService.uploadAttachment(file, currentUser);

        assertEquals(31L, result.getUploadId());
        assertEquals("brief.pdf", result.getAttachmentName());
        assertEquals("application/pdf", result.getAttachmentMimeType());
        assertEquals(5, result.getAttachmentSizeBytes());
        assertEquals("chat/1/2026-07-08/object-brief.pdf", result.getAttachmentStorageKey());
        assertEquals("https://cdn.example.test/chat/1/2026-07-08/object-brief.pdf", result.getAttachmentUrl());
    }

    @Test
    void getAttachmentStorageStatusReflectsLocalProviderConfiguration() {
        AppUser currentUser = createUser(1L, "mia");
        when(objectStorageProperties.isEnabled()).thenReturn(true);
        when(objectStorageProperties.getProvider()).thenReturn("local");
        when(objectStorageProperties.getLocalProxyBaseUrl()).thenReturn("/chat/attachments/object");
        when(objectStorageProperties.getLocalBasePath()).thenReturn("/tmp/themuffinman-object-storage");

        var result = chatService.getAttachmentStorageStatus(currentUser);

        assertEquals(true, result.isEnabled());
        assertEquals("local", result.getProvider());
        assertEquals("local-disk", result.getMode());
        assertEquals("/chat/attachments/object", result.getEndpoint());
    }

    @Test
    void markConversationReadMarksIncomingMessages() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        ChatMessage unreadMessage = new ChatMessage();
        unreadMessage.setConversation(conversation);
        unreadMessage.setSender(otherUser);
        unreadMessage.setId(9L);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatConversationStateService.markSeen(eq(conversation), eq(currentUser), eq((Long) null), any())).thenAnswer(invocation -> {
            unreadMessage.setReadAt(Instant.now());
            unreadMessage.setSeenAt(unreadMessage.getReadAt());
            return unreadMessage.getId();
        });
        when(chatPresenceService.isOnline(any(), any())).thenReturn(false);
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 1L)).thenReturn(List.of());

        chatService.markConversationRead(5L, null, currentUser);

        assertEquals(false, unreadMessage.getReadAt() == null);
        verify(chatConversationStateService).markSeen(eq(conversation), eq(currentUser), eq((Long) null), any());
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
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatPresenceRepository.findByUserIds(List.of(otherUser.getId()))).thenReturn(List.of(presence));
        when(chatPresenceService.isOnline(any(), any())).thenReturn(true);

        ChatWorkspaceDTO result = chatService.getWorkspace(currentUser);

        assertEquals(1, result.getContacts().size());
        assertEquals(1, result.getConversations().size());
        assertEquals(1, result.getUnreadConversationCount());
        assertEquals(1, result.getOnlineContactCount());
        assertEquals("Family", result.getCircles().getFirst().getName());
        assertEquals("chat-contact:2", result.getContacts().getFirst().getResolutionKey());
        assertEquals("conversation:5", result.getConversations().getFirst().getResolutionKey());
    }

    @Test
    void getWorkspaceExcludesContactsAndConversationsWithoutCurrentAcceptedRelation() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser staleUser = createUser(2L, "nikol");
        ChatConversation staleConversation = createConversation(5L, currentUser, staleUser);

        CircleGroup family = new CircleGroup();
        family.setId(11L);
        family.setName("Family");

        CircleMembership staleMembership = new CircleMembership();
        staleMembership.setCircle(family);
        staleMembership.setMember(staleUser);

        when(chatConversationRepository.findDetailedByParticipantId(currentUser.getId())).thenReturn(List.of(staleConversation));
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(), currentUser.getId())).thenReturn(List.of());
        when(circleMembershipService.getMembershipsByUserIdForOwner(currentUser.getId())).thenReturn(Map.of(staleUser.getId(), List.of(staleMembership)));
        when(circleRelationService.isCircleBetween(eq(currentUser), eq(staleUser))).thenReturn(false);

        ChatWorkspaceDTO result = chatService.getWorkspace(currentUser);

        assertEquals(0, result.getContacts().size());
        assertEquals(0, result.getConversations().size());
        assertEquals(0, result.getUnreadConversationCount());
        assertEquals(0, result.getOnlineContactCount());
        assertEquals(0, result.getCircles().size());
    }

    @Test
    void listConversationsFiltersByConversationTypeContextAndQuery() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser groupMember = createUser(2L, "john");
        AppUser questMember = createUser(3L, "lea");

        ChatConversation directConversation = createConversation(5L, currentUser, groupMember);
        directConversation.setLastMessageAt(Instant.now());

        ChatConversation groupConversation = createGroupConversation(6L, currentUser, groupMember, questMember);
        groupConversation.setConversationType(ChatConversationType.GROUP);
        groupConversation.setTitle("Weekend plans");
        groupConversation.setLastMessageAt(Instant.now().minusSeconds(5));

        ChatConversation questConversation = createGroupConversation(7L, currentUser, questMember);
        questConversation.setConversationType(ChatConversationType.QUEST_THREAD);
        questConversation.setContextType(com.themuffinman.app.chat.model.ChatConversationContextType.QUEST);
        questConversation.setContextId(21L);
        questConversation.setTitle("Fix sink");
        questConversation.setLastMessageAt(Instant.now().minusSeconds(10));

        Quest quest = new Quest();
        quest.setId(21L);
        quest.setCreator(currentUser);

        when(chatConversationRepository.findDetailedByParticipantId(currentUser.getId()))
                .thenReturn(List.of(groupConversation, questConversation, directConversation));
        when(circleRelationService.isCircleBetween(currentUser, groupMember)).thenReturn(true);
        when(workmarketQuestExecutionPrimitiveService.resolveTarget(21L)).thenReturn(quest);
        when(workmarketQuestAccessPolicyService.canManageQuest(quest, currentUser)).thenReturn(true);
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(7L), currentUser.getId())).thenReturn(List.of());

        ChatConversationListDTO result = chatService.listConversations(
                currentUser,
                "QUEST_THREAD",
                "QUEST",
                21L,
                "sink",
                10,
                0,
                false
        );

        assertEquals(1, result.getFilteredCount());
        assertEquals(1, result.getConversations().size());
        assertEquals(7L, result.getConversations().getFirst().getConversationId());
        assertEquals("QUEST_THREAD", result.getConversationType());
        assertEquals("QUEST", result.getContextType());
        assertEquals(21L, result.getContextId());
        assertEquals("sink", result.getQuery());
        assertEquals(0, result.getPage());
        assertFalse(result.isHasMore());
    }

    @Test
    void listConversationsRejectsContextIdWithoutContextType() {
        AppUser currentUser = createUser(1L, "mia");

        assertThrows(ResponseStatusException.class, () -> chatService.listConversations(
                currentUser,
                null,
                null,
                21L,
                null,
                10,
                0,
                false
        ));
    }

    @Test
    void listConversationsSupportsPageOffsets() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser participantOne = createUser(2L, "john");
        AppUser participantTwo = createUser(3L, "lea");
        AppUser participantThree = createUser(4L, "mark");

        ChatConversation first = createConversation(5L, currentUser, participantOne);
        first.setLastMessageAt(Instant.now());
        ChatConversation second = createConversation(6L, currentUser, participantTwo);
        second.setLastMessageAt(Instant.now().minusSeconds(10));
        ChatConversation third = createConversation(7L, currentUser, participantThree);
        third.setLastMessageAt(Instant.now().minusSeconds(20));

        when(chatConversationRepository.findDetailedByParticipantId(currentUser.getId()))
                .thenReturn(List.of(first, second, third));
        when(circleRelationService.isCircleBetween(currentUser, participantOne)).thenReturn(true);
        when(circleRelationService.isCircleBetween(currentUser, participantTwo)).thenReturn(true);
        when(circleRelationService.isCircleBetween(currentUser, participantThree)).thenReturn(true);
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(7L), currentUser.getId())).thenReturn(List.of());

        ChatConversationListDTO result = chatService.listConversations(
                currentUser,
                null,
                null,
                null,
                null,
                1,
                2,
                false
        );

        assertEquals(3, result.getFilteredCount());
        assertEquals(1, result.getConversations().size());
        assertEquals(7L, result.getConversations().getFirst().getConversationId());
        assertEquals(2, result.getPage());
        assertFalse(result.isHasMore());
    }

    @Test
    void addReactionReturnsMessageWithReactionList() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        ChatMessage message = buildMessage(8L, conversation, otherUser, "Hello", null);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatMessageRepository.findDetailedById(8L)).thenReturn(Optional.of(message));
        when(chatMessageReactionRepository.findDetailedByMessageIdAndUserIdAndEmoji(8L, 1L, "🔥")).thenReturn(Optional.empty());
        when(chatMessageReactionRepository.save(any(ChatMessageReaction.class))).thenAnswer(invocation -> {
            ChatMessageReaction reaction = invocation.getArgument(0);
            reaction.setId(12L);
            return reaction;
        });
        when(chatMessageReactionRepository.findDetailedByMessageIdIn(List.of(8L))).thenAnswer(invocation -> {
            ChatMessageReaction reaction = new ChatMessageReaction();
            reaction.setId(12L);
            reaction.setMessage(message);
            reaction.setUser(currentUser);
            reaction.setEmoji("🔥");
            reaction.setCreatedAt(Instant.parse("2026-07-08T10:00:00Z"));
            return List.of(reaction);
        });
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 1L)).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 2L)).thenReturn(List.of());

        var result = chatService.addReaction(
                5L,
                8L,
                com.themuffinman.app.chat.dto.ChatMessageReactionRequestDTO.builder().emoji("🔥").build(),
                currentUser
        );

        assertEquals(1, result.getReactions().size());
        assertEquals("🔥", result.getReactions().getFirst().getEmoji());
        assertEquals(true, result.getReactions().getFirst().isOwnReaction());
        verify(chatRealtimeService).notifyMessageUpdated(eq(conversation), eq(currentUser.getId()), any(), any());
        verify(chatAuditService).record(eq(com.themuffinman.app.chat.model.ChatAuditEventType.MESSAGE_REACTION_ADDED), eq(currentUser), eq(conversation), isNull(), isNull(), isNull(), any());
    }

    @Test
    void getConversationSyncReturnsTypingAndMessagesAfterCursor() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        conversation.setLastMessageId(12L);
        ChatMessage message = buildMessage(10L, conversation, otherUser, "Sync me", null);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatMessageRepository.findDetailedSinceMessageIdByConversationId(eq(5L), eq(9L), any()))
                .thenReturn(List.of(message));
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 1L)).thenReturn(List.of());
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatRealtimeService.getActiveTypingUserIds(5L, 1L, 8)).thenReturn(List.of(2L));

        var result = chatService.getConversationSync(5L, 9L, 20, currentUser);

        assertEquals(9L, result.getAfterMessageId());
        assertEquals(12L, result.getLatestMessageId());
        assertEquals(1, result.getMessages().size());
        assertEquals(List.of(2L), result.getActiveTypingUserIds());
    }

    @Test
    void heartbeatCreatesOrUpdatesPresenceRow() {
        AppUser currentUser = createUser(1L, "mia");

        chatService.heartbeat(currentUser);

        verify(chatPresenceService).markActive(currentUser);
    }

    @Test
    void redactExpiredImagesRemovesImageAndRefreshesConversationPreview() {
        RetentionProperties.Chat chatRetention = new RetentionProperties.Chat();
        chatRetention.setImageDays(30);
        chatRetention.setExpiredImagePlaceholder("Image expired");
        when(retentionProperties.getChat()).thenReturn(chatRetention);

        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);

        ChatMessage latestMessage = new ChatMessage();
        latestMessage.setConversation(conversation);
        latestMessage.setSender(otherUser);
        latestMessage.setMessageBody("Image expired");
        latestMessage.setImageDataUrl(null);

        when(chatMessageRepository.findConversationIdsWithExpiredImages(any())).thenReturn(List.of(5L));
        when(chatMessageRepository.redactExpiredImages(any(), any())).thenReturn(2);
        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(chatMessageRepository.findLatestDetailedByConversationId(org.mockito.ArgumentMatchers.eq(5L), any(Pageable.class)))
                .thenReturn(List.of(latestMessage));

        int updatedCount = chatService.redactExpiredImages();

        assertEquals(2, updatedCount);
        assertEquals("Image expired", conversation.getLastMessagePreview());
        assertFalse(conversation.isLastMessageHasImage());
        verify(chatConversationRepository).save(conversation);
    }

    @Test
    void sendMessageReturnsExistingMessageForDuplicateClientMessageId() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        ChatMessage existing = buildMessage(8L, conversation, currentUser, "Hello there from the chat", null);
        existing.setClientMessageId("abc-123");

        ChatMessageRequestDTO request = ChatMessageRequestDTO.builder()
                .messageBody("Hello there from the chat")
                .clientMessageId("abc-123")
                .build();

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatMessageRepository.findByConversationIdAndSenderIdAndClientMessageId(5L, 1L, "abc-123")).thenReturn(Optional.of(existing));

        var result = chatService.sendMessage(5L, request, currentUser);

        assertEquals(8L, result.getId());
        verify(chatMessageRepository).findByConversationIdAndSenderIdAndClientMessageId(5L, 1L, "abc-123");
    }

    @Test
    void markConversationReadCanTargetSpecificMessageId() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        ChatMessage unreadMessage = new ChatMessage();
        unreadMessage.setId(12L);
        unreadMessage.setConversation(conversation);
        unreadMessage.setSender(otherUser);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatConversationStateService.markSeen(eq(conversation), eq(currentUser), eq(12L), any())).thenAnswer(invocation -> {
            unreadMessage.setReadAt(Instant.now());
            unreadMessage.setSeenAt(unreadMessage.getReadAt());
            return unreadMessage.getId();
        });
        when(chatPresenceService.isOnline(any(), any())).thenReturn(false);
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 1L)).thenReturn(List.of());

        chatService.markConversationRead(5L, ChatMarkReadRequestDTO.builder().upToMessageId(12L).build(), currentUser);

        assertFalse(unreadMessage.getReadAt() == null);
        verify(chatRealtimeService).notifyConversationSeen(eq(conversation), eq(currentUser.getId()), any(), eq(12L));
    }

    @Test
    void updateConversationStateArchivesConversationForCurrentUser() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);
        ChatConversationMemberState state = new ChatConversationMemberState();
        state.setConversation(conversation);
        state.setUser(currentUser);
        state.setArchivedAt(Instant.parse("2026-07-08T08:00:00Z"));

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatConversationStateService.updateConversationState(eq(conversation), eq(currentUser), any(), any())).thenReturn(state);
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 1L)).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 2L)).thenReturn(List.of());

        var result = chatService.updateConversationState(
                5L,
                ChatConversationStateRequestDTO.builder().archived(true).build(),
                currentUser
        );

        assertEquals(true, result.isArchived());
        verify(chatRealtimeService).notifyConversationStateUpdated(eq(conversation), eq(currentUser.getId()), any());
    }

    @Test
    void markConversationDeliveredNotifiesRealtimeWhenReceiptsAdvance() {
        AppUser currentUser = createUser(1L, "mia");
        AppUser otherUser = createUser(2L, "john");
        ChatConversation conversation = createConversation(5L, currentUser, otherUser);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(circleRelationService.isCircleBetween(currentUser, otherUser)).thenReturn(true);
        when(chatConversationStateService.markDelivered(eq(conversation), eq(currentUser), eq(12L), any())).thenReturn(12L);
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 1L)).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), 2L)).thenReturn(List.of());

        chatService.markConversationDelivered(5L, ChatReceiptRequestDTO.builder().upToMessageId(12L).build(), currentUser);

        verify(chatRealtimeService).notifyConversationDelivered(eq(conversation), eq(currentUser.getId()), any(), eq(12L));
    }

    @Test
    void getCircleRoomReturnsExistingContextConversation() {
        AppUser owner = createUser(1L, "mia");
        AppUser member = createUser(2L, "john");
        ChatConversation conversation = createGroupConversation(5L, owner, member);
        conversation.setConversationType(ChatConversationType.CIRCLE_ROOM);
        conversation.setContextType(com.themuffinman.app.chat.model.ChatConversationContextType.CIRCLE);
        conversation.setContextId(11L);

        CircleGroup circle = new CircleGroup();
        circle.setId(11L);
        circle.setName("Family");
        circle.setOwner(owner);

        when(circleGroupRepository.findById(11L)).thenReturn(Optional.of(circle));
        when(circleMembershipService.isCircleMember(11L, member.getId())).thenReturn(true);
        when(chatConversationRepository.findDetailedByContextTypeAndContextId(com.themuffinman.app.chat.model.ChatConversationContextType.CIRCLE, 11L))
                .thenReturn(Optional.of(conversation));
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), member.getId())).thenReturn(List.of());

        var result = chatService.getCircleRoom(11L, member);

        assertEquals(5L, result.getConversationId());
        assertEquals("CIRCLE", result.getContextType());
    }

    @Test
    void getQuestThreadReturnsExistingContextConversation() {
        AppUser owner = createUser(1L, "mia");
        AppUser worker = createUser(2L, "john");
        ChatConversation conversation = createGroupConversation(5L, owner, worker);
        conversation.setConversationType(ChatConversationType.QUEST_THREAD);
        conversation.setContextType(com.themuffinman.app.chat.model.ChatConversationContextType.QUEST);
        conversation.setContextId(21L);

        Quest quest = new Quest();
        quest.setId(21L);
        quest.setCreator(owner);
        quest.setTitle("Fix sink");

        when(workmarketQuestExecutionPrimitiveService.resolveTarget(21L)).thenReturn(quest);
        when(workmarketQuestAccessPolicyService.canManageQuest(quest, worker)).thenReturn(false);
        when(workmarketQuestApplicationRepository.findByQuestIdAndApplicantIdAndStatus(21L, worker.getId(), com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED))
                .thenReturn(Optional.of(new QuestApplication()));
        when(chatConversationRepository.findDetailedByContextTypeAndContextId(com.themuffinman.app.chat.model.ChatConversationContextType.QUEST, 21L))
                .thenReturn(Optional.of(conversation));
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), worker.getId())).thenReturn(List.of());

        var result = chatService.getQuestThread(21L, worker);

        assertEquals(5L, result.getConversationId());
        assertEquals("QUEST", result.getContextType());
    }

    @Test
    void getApplicationThreadReturnsExistingContextConversation() {
        AppUser owner = createUser(1L, "mia");
        AppUser applicant = createUser(2L, "john");
        ChatConversation conversation = createGroupConversation(5L, owner, applicant);
        conversation.setConversationType(ChatConversationType.APPLICATION_THREAD);
        conversation.setContextType(com.themuffinman.app.chat.model.ChatConversationContextType.APPLICATION);
        conversation.setContextId(31L);

        Quest quest = new Quest();
        quest.setId(21L);
        quest.setCreator(owner);
        quest.setTitle("Fix sink");

        QuestApplication application = new QuestApplication();
        application.setId(31L);
        application.setQuest(quest);
        application.setApplicant(applicant);

        when(workmarketQuestApplicationRepository.findByIdDetailed(31L)).thenReturn(Optional.of(application));
        when(workmarketQuestAccessPolicyService.canViewQuestApplication(application, quest, applicant)).thenReturn(true);
        when(chatConversationRepository.findDetailedByContextTypeAndContextId(com.themuffinman.app.chat.model.ChatConversationContextType.APPLICATION, 31L))
                .thenReturn(Optional.of(conversation));
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), applicant.getId())).thenReturn(List.of());

        var result = chatService.getApplicationThread(31L, applicant);

        assertEquals(5L, result.getConversationId());
        assertEquals("APPLICATION", result.getContextType());
    }

    @Test
    void renameGroupConversationAllowsAdminManager() {
        AppUser owner = createUser(1L, "mia");
        AppUser admin = createUser(2L, "john");
        AppUser member = createUser(3L, "lea");
        ChatConversation conversation = createGroupConversation(5L, owner, admin, member);
        setParticipantRole(conversation, admin.getId(), ChatConversationParticipantRole.ADMIN);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), admin.getId())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), owner.getId())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), member.getId())).thenReturn(List.of());

        var result = chatService.renameGroupConversation(
                5L,
                ChatConversationTitleRequestDTO.builder().title("Project Room").build(),
                admin
        );

        assertEquals("Project Room", result.getTitle());
        assertEquals("Project Room", conversation.getTitle());
        verify(chatRealtimeService).notifyConversationStateUpdated(eq(conversation), eq(admin.getId()), any());
    }

    @Test
    void addGroupParticipantsAddsNewCircleContact() {
        AppUser owner = createUser(1L, "mia");
        AppUser member = createUser(2L, "john");
        AppUser memberTwo = createUser(3L, "lea");
        AppUser newParticipant = createUser(4L, "sara");
        ChatConversation conversation = createGroupConversation(5L, owner, member, memberTwo);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(appUserLookupService.requireById(4L)).thenReturn(newParticipant);
        when(circleRelationService.isCircleBetween(owner, newParticipant)).thenReturn(true);
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), owner.getId())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), member.getId())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), memberTwo.getId())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), newParticipant.getId())).thenReturn(List.of());

        var result = chatService.addGroupParticipants(
                5L,
                ChatConversationParticipantsRequestDTO.builder().participantUserIds(List.of(4L)).build(),
                owner
        );

        assertEquals(4, result.getParticipantCount());
        assertEquals(4, conversation.getParticipants().size());
        verify(chatRealtimeService).notifyConversationStateUpdated(eq(conversation), eq(owner.getId()), any());
    }

    @Test
    void updateGroupParticipantRoleTransfersOwnership() {
        AppUser owner = createUser(1L, "mia");
        AppUser admin = createUser(2L, "john");
        AppUser member = createUser(3L, "lea");
        ChatConversation conversation = createGroupConversation(5L, owner, admin, member);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), owner.getId())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), admin.getId())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), member.getId())).thenReturn(List.of());

        var result = chatService.updateGroupParticipantRole(
                5L,
                admin.getId(),
                ChatConversationRoleRequestDTO.builder().role("OWNER").build(),
                owner
        );

        assertEquals(admin.getId(), result.getOwnerUserId());
        assertEquals(admin.getId(), conversation.getOwner().getId());
        assertEquals(ChatConversationParticipantRole.ADMIN, requireParticipantRole(conversation, owner.getId()));
        assertEquals(ChatConversationParticipantRole.OWNER, requireParticipantRole(conversation, admin.getId()));
    }

    @Test
    void adminCannotTransferOwnership() {
        AppUser owner = createUser(1L, "mia");
        AppUser admin = createUser(2L, "john");
        AppUser member = createUser(3L, "lea");
        ChatConversation conversation = createGroupConversation(5L, owner, admin, member);
        setParticipantRole(conversation, admin.getId(), ChatConversationParticipantRole.ADMIN);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));

        assertThrows(ResponseStatusException.class, () -> chatService.updateGroupParticipantRole(
                5L,
                member.getId(),
                ChatConversationRoleRequestDTO.builder().role("OWNER").build(),
                admin
        ));

        verify(chatRealtimeService, never()).notifyConversationStateUpdated(any(), any(), any());
    }

    @Test
    void leaveConversationTransfersOwnershipToAdmin() {
        AppUser owner = createUser(1L, "mia");
        AppUser admin = createUser(2L, "john");
        AppUser member = createUser(3L, "lea");
        ChatConversation conversation = createGroupConversation(5L, owner, admin, member);
        setParticipantRole(conversation, admin.getId(), ChatConversationParticipantRole.ADMIN);

        when(chatConversationRepository.findDetailedById(5L)).thenReturn(Optional.of(conversation));
        when(chatPresenceRepository.findByUserIds(any())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), admin.getId())).thenReturn(List.of());
        when(chatMessageRepository.findUnreadCountsByConversationIds(List.of(5L), member.getId())).thenReturn(List.of());

        chatService.leaveConversation(5L, owner);

        assertEquals(admin.getId(), conversation.getOwner().getId());
        assertEquals(2, conversation.getParticipants().size());
        verify(chatRealtimeService).notifyWorkspaceChanged(owner.getId(), 5L, owner.getId(), "conversation_membership_updated");
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
        conversation.setConversationType(ChatConversationType.DIRECT);
        conversation.setLeftParticipant(leftParticipant);
        conversation.setRightParticipant(rightParticipant);
        ChatConversationParticipant left = new ChatConversationParticipant();
        left.setConversation(conversation);
        left.setUser(leftParticipant);
        left.setRole(ChatConversationParticipantRole.OWNER);
        ChatConversationParticipant right = new ChatConversationParticipant();
        right.setConversation(conversation);
        right.setUser(rightParticipant);
        right.setRole(ChatConversationParticipantRole.MEMBER);
        conversation.getParticipants().add(left);
        conversation.getParticipants().add(right);
        return conversation;
    }

    private ChatConversation createGroupConversation(Long id, AppUser owner, AppUser... participants) {
        ChatConversation conversation = new ChatConversation();
        conversation.setId(id);
        conversation.setConversationType(ChatConversationType.GROUP);
        conversation.setOwner(owner);
        conversation.setCreatedBy(owner);
        conversation.setParticipants(new java.util.LinkedHashSet<>());
        List<AppUser> allParticipants = new ArrayList<>();
        allParticipants.add(owner);
        allParticipants.addAll(List.of(participants));
        long participantId = 100L;
        for (AppUser participantUser : allParticipants) {
            ChatConversationParticipant participant = new ChatConversationParticipant();
            participant.setId(participantId++);
            participant.setConversation(conversation);
            participant.setUser(participantUser);
            participant.setRole(Objects.equals(participantUser.getId(), owner.getId())
                    ? ChatConversationParticipantRole.OWNER
                    : ChatConversationParticipantRole.MEMBER);
            participant.setAddedBy(owner);
            conversation.getParticipants().add(participant);
        }
        return conversation;
    }

    private void setParticipantRole(ChatConversation conversation, Long userId, ChatConversationParticipantRole role) {
        conversation.getParticipants().stream()
                .filter(participant -> Objects.equals(participant.getUser().getId(), userId))
                .findFirst()
                .orElseThrow()
                .setRole(role);
    }

    private ChatConversationParticipantRole requireParticipantRole(ChatConversation conversation, Long userId) {
        return conversation.getParticipants().stream()
                .filter(participant -> Objects.equals(participant.getUser().getId(), userId))
                .findFirst()
                .orElseThrow()
                .getRole();
    }

    private ChatMessage buildMessage(Long id, ChatConversation conversation, AppUser sender, String messageBody, String imageDataUrl) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setConversation(conversation);
        message.setSender(sender);
        message.setMessageBody(messageBody);
        message.setImageDataUrl(imageDataUrl);
        return message;
    }
}
