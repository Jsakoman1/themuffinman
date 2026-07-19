package com.themuffinman.app.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.chat.dto.ChatConversationListDTO;
import com.themuffinman.app.chat.dto.ChatConversationParticipantsRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatConversationSyncDTO;
import com.themuffinman.app.chat.dto.ChatAttachmentUploadDTO;
import com.themuffinman.app.chat.dto.ChatAttachmentStorageStatusDTO;
import com.themuffinman.app.chat.dto.ChatConversationRoleRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationStateRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationTitleRequestDTO;
import com.themuffinman.app.chat.dto.ChatMarkReadRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import com.themuffinman.app.chat.dto.ChatMessagePageDTO;
import com.themuffinman.app.chat.dto.ChatReceiptRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageUpdateRequestDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.common.controller.GlobalExceptionHandler;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.storage.StoredObjectPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

class ChatControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatService chatService = mock(ChatService.class);
    private final com.themuffinman.app.chat.service.ChatSyncService chatSyncService = mock(com.themuffinman.app.chat.service.ChatSyncService.class);
    private MockMvc mockMvc;
    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ChatController controller = new ChatController(chatService, chatSyncService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();
        currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("mia");
        currentUser.setEmail("mia@sidequest.test");
        currentUser.setPasswordHash("hash");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getConversationMessagesSupportsPaginationParams() throws Exception {
        authenticateCurrentUser();
        ChatMessagePageDTO page = ChatMessagePageDTO.builder()
                .messages(List.of(ChatMessageDTO.builder().id(11L).conversationId(5L).senderUserId(1L).senderUsername("mia")
                        .createdAt("2026-07-08T08:00:00Z").updatedAt("2026-07-08T08:00:00Z").ownMessage(true).edited(false).deleted(false).build()))
                .limit(20)
                .hasMore(true)
                .nextBeforeMessageId(11L)
                .build();
        when(chatService.getConversationMessages(5L, 12L, 20, currentUser)).thenReturn(page);

        mockMvc.perform(get("/chat/conversations/5/messages")
                        .param("beforeMessageId", "12")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(jsonPath("$.nextBeforeMessageId").value(11));

        verify(chatService).getConversationMessages(5L, 12L, 20, currentUser);
    }

    @Test
    void getConversationSyncDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.getConversationSync(5L, 9L, 20, currentUser))
                .thenReturn(ChatConversationSyncDTO.builder()
                        .conversation(ChatConversationSummaryDTO.builder().conversationId(5L).build())
                        .messages(List.of())
                        .afterMessageId(9L)
                        .latestMessageId(12L)
                        .activeTypingUserIds(List.of(2L))
                        .typingTimeoutSeconds(8)
                        .build());

        mockMvc.perform(get("/chat/conversations/5/sync")
                        .param("afterMessageId", "9")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.afterMessageId").value(9))
                .andExpect(jsonPath("$.latestMessageId").value(12))
                .andExpect(jsonPath("$.activeTypingUserIds[0]").value(2));

        verify(chatService).getConversationSync(5L, 9L, 20, currentUser);
    }

    @Test
    void uploadAttachmentDelegatesToService() throws Exception {
        authenticateCurrentUser();
        MockMultipartFile file = new MockMultipartFile("file", "brief.pdf", "application/pdf", "hello".getBytes());
        when(chatService.uploadAttachment(org.mockito.ArgumentMatchers.any(), eq(currentUser)))
                .thenReturn(ChatAttachmentUploadDTO.builder()
                        .uploadId(31L)
                        .attachmentName("brief.pdf")
                        .attachmentMimeType("application/pdf")
                        .attachmentSizeBytes(5)
                        .attachmentStorageProvider("s3")
                        .attachmentStorageKey("chat/1/2026-07-08/object-brief.pdf")
                        .attachmentUrl("https://cdn.example.test/chat/1/2026-07-08/object-brief.pdf")
                        .build());

        mockMvc.perform(multipart("/chat/attachments").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value(31))
                .andExpect(jsonPath("$.attachmentName").value("brief.pdf"))
                .andExpect(jsonPath("$.attachmentMimeType").value("application/pdf"))
                .andExpect(jsonPath("$.attachmentSizeBytes").value(5))
                .andExpect(jsonPath("$.attachmentStorageKey").value("chat/1/2026-07-08/object-brief.pdf"));
    }

    @Test
    void getAttachmentStorageStatusDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.getAttachmentStorageStatus(currentUser)).thenReturn(ChatAttachmentStorageStatusDTO.builder()
                .enabled(true)
                .provider("local")
                .mode("local-disk")
                .endpoint("/chat/attachments/object")
                .localBasePath("/tmp/themuffinman-object-storage")
                .build());

        mockMvc.perform(get("/chat/attachments/storage-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(jsonPath("$.mode").value("local-disk"));
    }

    @Test
    void getAttachmentObjectDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.getAttachmentObject("chat/1/file.pdf", currentUser))
                .thenReturn(new StoredObjectPayload("local", "chat/1/file.pdf", "application/pdf", "hello".getBytes()));

        mockMvc.perform(get("/chat/attachments/object")
                        .param("key", "chat/1/file.pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes("hello".getBytes()));
    }

    @Test
    void getWorkspacePassesOptionalConversationLimit() throws Exception {
        authenticateCurrentUser();
        when(chatService.getWorkspace(currentUser, 15, false)).thenReturn(ChatWorkspaceDTO.builder()
                .conversations(List.of())
                .contacts(List.of())
                .circles(List.of())
                .unreadConversationCount(0)
                .onlineContactCount(0)
                .conversationLimit(15)
                .build());

        mockMvc.perform(get("/chat/workspace")
                        .param("conversationLimit", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationLimit").value(15));

        verify(chatService).getWorkspace(currentUser, 15, false);
    }

    @Test
    void getWorkspacePassesIncludeArchivedFlag() throws Exception {
        authenticateCurrentUser();
        when(chatService.getWorkspace(currentUser, 20, true)).thenReturn(ChatWorkspaceDTO.builder()
                .conversations(List.of())
                .contacts(List.of())
                .circles(List.of())
                .unreadConversationCount(0)
                .onlineContactCount(0)
                .conversationLimit(20)
                .build());

        mockMvc.perform(get("/chat/workspace")
                        .param("conversationLimit", "20")
                        .param("includeArchived", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationLimit").value(20));

        verify(chatService).getWorkspace(currentUser, 20, true);
    }

    @Test
    void listConversationsPassesFiltersToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.listConversations(currentUser, "GROUP", "QUEST", 21L, "fix", 10, 1, null, null, true))
                .thenReturn(ChatConversationListDTO.builder()
                        .conversations(List.of())
                        .filteredCount(0)
                        .limit(10)
                        .page(1)
                        .hasMore(false)
                        .includeArchived(true)
                        .conversationType("GROUP")
                        .contextType("QUEST")
                        .contextId(21L)
                        .query("fix")
                        .build());

        mockMvc.perform(get("/chat/conversations")
                        .param("conversationType", "GROUP")
                        .param("contextType", "QUEST")
                        .param("contextId", "21")
                        .param("query", "fix")
                        .param("limit", "10")
                        .param("page", "1")
                        .param("includeArchived", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.includeArchived").value(true))
                .andExpect(jsonPath("$.conversationType").value("GROUP"))
                .andExpect(jsonPath("$.contextType").value("QUEST"))
                .andExpect(jsonPath("$.contextId").value(21))
                .andExpect(jsonPath("$.query").value("fix"));
    }

    @Test
    void listConversationsPassesCursorToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.listConversations(currentUser, null, null, null, null, 10, null, "2026-07-10T10:00:00Z", 42L, false))
                .thenReturn(ChatConversationListDTO.builder()
                        .conversations(List.of())
                        .filteredCount(0)
                        .limit(10)
                        .page(0)
                        .hasMore(false)
                        .includeArchived(false)
                        .build());

        mockMvc.perform(get("/chat/conversations")
                        .param("limit", "10")
                        .param("beforeLastMessageAt", "2026-07-10T10:00:00Z")
                        .param("beforeConversationId", "42"))
                .andExpect(status().isOk());

        verify(chatService).listConversations(currentUser, null, null, null, null, 10, null, "2026-07-10T10:00:00Z", 42L, false);
    }

    @Test
    void getCircleRoomDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.getCircleRoom(11L, currentUser))
                .thenReturn(ChatConversationSummaryDTO.builder().conversationId(5L).contextType("CIRCLE").contextId(11L).build());

        mockMvc.perform(get("/chat/circles/11/room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(5))
                .andExpect(jsonPath("$.contextType").value("CIRCLE"))
                .andExpect(jsonPath("$.contextId").value(11));
    }

    @Test
    void getQuestThreadDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.getQuestThread(12L, currentUser))
                .thenReturn(ChatConversationSummaryDTO.builder().conversationId(6L).contextType("QUEST").contextId(12L).build());

        mockMvc.perform(get("/chat/quests/12/thread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(6))
                .andExpect(jsonPath("$.contextType").value("QUEST"))
                .andExpect(jsonPath("$.contextId").value(12));
    }

    @Test
    void getApplicationThreadDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.getApplicationThread(13L, currentUser))
                .thenReturn(ChatConversationSummaryDTO.builder().conversationId(7L).contextType("APPLICATION").contextId(13L).build());

        mockMvc.perform(get("/chat/applications/13/thread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(7))
                .andExpect(jsonPath("$.contextType").value("APPLICATION"))
                .andExpect(jsonPath("$.contextId").value(13));
    }

    @Test
    void renameGroupConversationDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.renameGroupConversation(eq(5L), argThat(request ->
                        request != null && "Project room".equals(request.getTitle())), eq(currentUser)))
                .thenReturn(ChatConversationSummaryDTO.builder().conversationId(5L).title("Project room").build());

        mockMvc.perform(patch("/chat/conversations/5/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ChatConversationTitleRequestDTO.builder().title("Project room").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(5))
                .andExpect(jsonPath("$.title").value("Project room"));
    }

    @Test
    void addGroupParticipantsDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.addGroupParticipants(eq(5L), argThat(request ->
                        request != null && request.getParticipantUserIds().equals(List.of(2L, 3L))), eq(currentUser)))
                .thenReturn(ChatConversationSummaryDTO.builder().conversationId(5L).participantCount(4).build());

        mockMvc.perform(post("/chat/conversations/5/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ChatConversationParticipantsRequestDTO.builder()
                                .participantUserIds(List.of(2L, 3L))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(5))
                .andExpect(jsonPath("$.participantCount").value(4));
    }

    @Test
    void updateGroupParticipantRoleDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.updateGroupParticipantRole(eq(5L), eq(2L), argThat(request ->
                        request != null && "ADMIN".equals(request.getRole())), eq(currentUser)))
                .thenReturn(ChatConversationSummaryDTO.builder().conversationId(5L).ownerUserId(1L).build());

        mockMvc.perform(patch("/chat/conversations/5/participants/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ChatConversationRoleRequestDTO.builder().role("ADMIN").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(5));
    }

    @Test
    void removeGroupParticipantDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.removeGroupParticipant(5L, 2L, currentUser))
                .thenReturn(ChatConversationSummaryDTO.builder().conversationId(5L).participantCount(2).build());

        mockMvc.perform(delete("/chat/conversations/5/participants/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(5))
                .andExpect(jsonPath("$.participantCount").value(2));
    }

    @Test
    void leaveConversationDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.leaveConversation(5L, currentUser)).thenReturn(
                com.themuffinman.app.chat.dto.ChatMembershipTransitionDTO.builder()
                        .action("LEAVE_CHAT_CONVERSATION")
                        .transitionState("LEFT")
                        .conversationId(5L)
                        .message("Conversation left.")
                        .build()
        );

        mockMvc.perform(delete("/chat/conversations/5/participants/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("LEAVE_CHAT_CONVERSATION"));

        verify(chatService).leaveConversation(5L, currentUser);
    }

    @Test
    void updateMessageRejectsBlankBody() throws Exception {
        authenticateCurrentUser();
        mockMvc.perform(patch("/chat/conversations/5/messages/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ChatMessageUpdateRequestDTO.builder().messageBody(" ").build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("messageBody"));
    }

    @Test
    void deleteMessageDelegatesToService() throws Exception {
        authenticateCurrentUser();
        mockMvc.perform(delete("/chat/conversations/5/messages/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("DELETE_CHAT_MESSAGE"));

        verify(chatService).deleteMessage(5L, 7L, currentUser);
    }

    @Test
    void updateConversationStateDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatService.updateConversationState(eq(5L), argThat(request ->
                        request != null && Boolean.TRUE.equals(request.getArchived())), eq(currentUser)))
                .thenReturn(ChatConversationSummaryDTO.builder().conversationId(5L).archived(true).build());

        mockMvc.perform(patch("/chat/conversations/5/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ChatConversationStateRequestDTO.builder().archived(true).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(5))
                .andExpect(jsonPath("$.archived").value(true));
    }

    @Test
    void markConversationDeliveredAcceptsOptionalRequestBody() throws Exception {
        authenticateCurrentUser();
        mockMvc.perform(patch("/chat/conversations/5/delivery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ChatReceiptRequestDTO.builder().upToMessageId(9L).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("MARK_CHAT_CONVERSATION_DELIVERED"));

        verify(chatService).markConversationDelivered(
                eq(5L),
                argThat(request -> request != null && Long.valueOf(9L).equals(request.getUpToMessageId())),
                eq(currentUser)
        );
    }

    @Test
    void markConversationReadAcceptsOptionalRequestBody() throws Exception {
        authenticateCurrentUser();
        mockMvc.perform(patch("/chat/conversations/5/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ChatMarkReadRequestDTO.builder().upToMessageId(9L).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("MARK_CHAT_CONVERSATION_READ"));

        verify(chatService).markConversationRead(
                eq(5L),
                argThat(request -> request != null && Long.valueOf(9L).equals(request.getUpToMessageId())),
                eq(currentUser)
        );
    }

    @Test
    void markConversationReadAllowsMissingBody() throws Exception {
        authenticateCurrentUser();
        mockMvc.perform(patch("/chat/conversations/5/read"))
                .andExpect(status().isOk());

        verify(chatService).markConversationRead(eq(5L), isNull(), eq(currentUser));
    }

    private void authenticateCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(authentication());
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
