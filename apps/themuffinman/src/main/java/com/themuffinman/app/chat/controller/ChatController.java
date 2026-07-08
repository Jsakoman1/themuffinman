package com.themuffinman.app.chat.controller;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatConversationListDTO;
import com.themuffinman.app.chat.dto.ChatConversationSyncDTO;
import com.themuffinman.app.chat.dto.ChatConversationParticipantsRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationRoleRequestDTO;
import com.themuffinman.app.chat.dto.ChatCreateGroupConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationStateRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationTitleRequestDTO;
import com.themuffinman.app.chat.dto.ChatAttachmentUploadDTO;
import com.themuffinman.app.chat.dto.ChatAttachmentStorageStatusDTO;
import com.themuffinman.app.chat.dto.ChatMarkReadRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import com.themuffinman.app.chat.dto.ChatMessagePageDTO;
import com.themuffinman.app.chat.dto.ChatMessageReactionRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageUpdateRequestDTO;
import com.themuffinman.app.chat.dto.ChatOpenConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatReceiptRequestDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/workspace")
    public ChatWorkspaceDTO getWorkspace(
            @RequestParam(value = "conversationLimit", required = false) Integer conversationLimit,
            @RequestParam(value = "includeArchived", defaultValue = "false") boolean includeArchived,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.getWorkspace(currentUser, conversationLimit, includeArchived);
    }

    @GetMapping("/conversations")
    public ChatConversationListDTO listConversations(
            @RequestParam(value = "conversationType", required = false) String conversationType,
            @RequestParam(value = "contextType", required = false) String contextType,
            @RequestParam(value = "contextId", required = false) Long contextId,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "includeArchived", defaultValue = "false") boolean includeArchived,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.listConversations(currentUser, conversationType, contextType, contextId, query, limit, page, includeArchived);
    }

    @PostMapping("/conversations/open")
    public ChatConversationSummaryDTO openConversation(
            @Valid @RequestBody ChatOpenConversationRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.openConversation(request, currentUser);
    }

    @PostMapping("/conversations/groups")
    public ChatConversationSummaryDTO createGroupConversation(
            @Valid @RequestBody ChatCreateGroupConversationRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.createGroupConversation(request, currentUser);
    }

    @PatchMapping("/conversations/{conversationId}/group")
    public ChatConversationSummaryDTO renameGroupConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatConversationTitleRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.renameGroupConversation(conversationId, request, currentUser);
    }

    @PostMapping("/conversations/{conversationId}/participants")
    public ChatConversationSummaryDTO addGroupParticipants(
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatConversationParticipantsRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.addGroupParticipants(conversationId, request, currentUser);
    }

    @PatchMapping("/conversations/{conversationId}/participants/{participantUserId}/role")
    public ChatConversationSummaryDTO updateGroupParticipantRole(
            @PathVariable Long conversationId,
            @PathVariable Long participantUserId,
            @Valid @RequestBody ChatConversationRoleRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.updateGroupParticipantRole(conversationId, participantUserId, request, currentUser);
    }

    @DeleteMapping("/conversations/{conversationId}/participants/{participantUserId}")
    public ChatConversationSummaryDTO removeGroupParticipant(
            @PathVariable Long conversationId,
            @PathVariable Long participantUserId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.removeGroupParticipant(conversationId, participantUserId, currentUser);
    }

    @DeleteMapping("/conversations/{conversationId}/participants/me")
    public ActionResultDTO leaveConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        chatService.leaveConversation(conversationId, currentUser);
        return ActionResults.of("LEAVE_CHAT_CONVERSATION", "Conversation left.");
    }

    @PostMapping("/circles/{circleId}/room")
    public ChatConversationSummaryDTO openCircleRoom(
            @PathVariable Long circleId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.openCircleRoom(circleId, currentUser);
    }

    @GetMapping("/circles/{circleId}/room")
    public ChatConversationSummaryDTO getCircleRoom(
            @PathVariable Long circleId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.getCircleRoom(circleId, currentUser);
    }

    @PostMapping("/quests/{questId}/thread")
    public ChatConversationSummaryDTO openQuestThread(
            @PathVariable Long questId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.openQuestThread(questId, currentUser);
    }

    @GetMapping("/quests/{questId}/thread")
    public ChatConversationSummaryDTO getQuestThread(
            @PathVariable Long questId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.getQuestThread(questId, currentUser);
    }

    @PostMapping("/applications/{applicationId}/thread")
    public ChatConversationSummaryDTO openApplicationThread(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.openApplicationThread(applicationId, currentUser);
    }

    @GetMapping("/applications/{applicationId}/thread")
    public ChatConversationSummaryDTO getApplicationThread(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.getApplicationThread(applicationId, currentUser);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ChatMessagePageDTO getConversationMessages(
            @PathVariable Long conversationId,
            @RequestParam(value = "beforeMessageId", required = false) Long beforeMessageId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.getConversationMessages(conversationId, beforeMessageId, limit, currentUser);
    }

    @GetMapping("/conversations/{conversationId}/sync")
    public ChatConversationSyncDTO getConversationSync(
            @PathVariable Long conversationId,
            @RequestParam(value = "afterMessageId", required = false) Long afterMessageId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.getConversationSync(conversationId, afterMessageId, limit, currentUser);
    }

    @PostMapping("/attachments")
    public ChatAttachmentUploadDTO uploadAttachment(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.uploadAttachment(file, currentUser);
    }

    @GetMapping("/attachments/storage-status")
    public ChatAttachmentStorageStatusDTO getAttachmentStorageStatus(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.getAttachmentStorageStatus(currentUser);
    }

    @GetMapping("/attachments/object")
    public ResponseEntity<ByteArrayResource> getAttachmentObject(
            @RequestParam("key") String storageKey,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        var attachment = chatService.getAttachmentObject(storageKey, currentUser);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.contentType()))
                .contentLength(attachment.content().length)
                .body(new ByteArrayResource(attachment.content()));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ChatMessageDTO sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.sendMessage(conversationId, request, currentUser);
    }

    @PostMapping("/conversations/{conversationId}/messages/{messageId}/reactions")
    public ChatMessageDTO addReaction(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @Valid @RequestBody ChatMessageReactionRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.addReaction(conversationId, messageId, request, currentUser);
    }

    @DeleteMapping("/conversations/{conversationId}/messages/{messageId}/reactions/{emoji}")
    public ChatMessageDTO removeReaction(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @PathVariable String emoji,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.removeReaction(conversationId, messageId, emoji, currentUser);
    }

    @PatchMapping("/conversations/{conversationId}/messages/{messageId}")
    public ChatMessageDTO updateMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @Valid @RequestBody ChatMessageUpdateRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.updateMessage(conversationId, messageId, request, currentUser);
    }

    @DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
    public ActionResultDTO deleteMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        chatService.deleteMessage(conversationId, messageId, currentUser);
        return ActionResults.of("DELETE_CHAT_MESSAGE", "Message deleted.");
    }

    @PatchMapping("/conversations/{conversationId}/state")
    public ChatConversationSummaryDTO updateConversationState(
            @PathVariable Long conversationId,
            @RequestBody ChatConversationStateRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.updateConversationState(conversationId, request, currentUser);
    }

    @PatchMapping("/conversations/{conversationId}/delivery")
    public ActionResultDTO markConversationDelivered(
            @PathVariable Long conversationId,
            @RequestBody(required = false) ChatReceiptRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        chatService.markConversationDelivered(conversationId, request, currentUser);
        return ActionResults.of("MARK_CHAT_CONVERSATION_DELIVERED", "Conversation delivery updated.");
    }

    @PatchMapping("/conversations/{conversationId}/read")
    public ActionResultDTO markConversationRead(
            @PathVariable Long conversationId,
            @RequestBody(required = false) ChatMarkReadRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        chatService.markConversationRead(conversationId, request, currentUser);
        return ActionResults.of("MARK_CHAT_CONVERSATION_READ", "Conversation marked as read.");
    }

    @PostMapping("/presence/heartbeat")
    public ActionResultDTO heartbeat(@AuthenticationPrincipal AppUser currentUser) {
        chatService.heartbeat(currentUser);
        return ActionResults.of("CHAT_HEARTBEAT", "Presence updated.");
    }
}
