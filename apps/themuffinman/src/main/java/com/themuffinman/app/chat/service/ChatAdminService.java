package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatAdminConversationSupportViewDTO;
import com.themuffinman.app.chat.dto.ChatAuditEventDTO;
import com.themuffinman.app.chat.dto.ChatAuditEventListDTO;
import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import com.themuffinman.app.chat.model.ChatAuditEvent;
import com.themuffinman.app.chat.model.ChatAuditEventType;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.model.ChatMessage;
import com.themuffinman.app.chat.model.ChatMessageReaction;
import com.themuffinman.app.chat.repository.ChatAuditEventRepository;
import com.themuffinman.app.chat.repository.ChatConversationRepository;
import com.themuffinman.app.chat.repository.ChatMessageReactionRepository;
import com.themuffinman.app.chat.repository.ChatMessageRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.ChatProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.storage.ObjectStorageAccess;
import com.themuffinman.app.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatAdminService {

    private final ChatAuditEventRepository chatAuditEventRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReactionRepository chatMessageReactionRepository;
    private final ChatAuditService chatAuditService;
    private final ChatProperties chatProperties;
    private final ObjectStorageService objectStorageService;

    @Transactional(readOnly = true)
    public ChatAuditEventListDTO getAuditEvents(
            String requestedEventType,
            Long requestedUserId,
            Long requestedConversationId,
            Integer requestedLimit,
            AppUser currentUser
    ) {
        requireAdmin(currentUser);
        ChatAuditEventType eventType = parseEventType(requestedEventType);
        int limit = normalizeAuditLimit(requestedLimit);
        List<ChatAuditEventDTO> events = chatAuditEventRepository.findDetailedByFilters(
                        eventType,
                        requestedUserId,
                        requestedConversationId,
                        PageRequest.of(0, limit)
                ).stream()
                .map(this::toAuditEventDto)
                .toList();
        return ChatAuditEventListDTO.builder()
                .events(events)
                .limit(limit)
                .eventType(eventType == null ? null : eventType.name())
                .userId(requestedUserId)
                .conversationId(requestedConversationId)
                .build();
    }

    @Transactional(readOnly = true)
    public ChatAdminConversationSupportViewDTO getConversationSupportView(Long conversationId, AppUser currentUser) {
        requireAdmin(currentUser);
        ChatConversation conversation = chatConversationRepository.findDetailedById(conversationId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat conversation not found with id " + conversationId));
        int recentMessagesLimit = Math.max(chatProperties.getSupport().getRecentMessagesLimit(), 1);
        int recentAuditLimit = Math.max(chatProperties.getSupport().getRecentAuditLimit(), 1);
        List<ChatMessage> recentMessages = chatMessageRepository.findLatestDetailedByConversationId(
                conversationId,
                PageRequest.of(0, recentMessagesLimit)
        );
        Map<Long, List<ChatMessageReaction>> reactionsByMessageId = groupReactionsByMessageId(recentMessages);
        List<ChatAuditEventDTO> recentAuditEvents = chatAuditEventRepository.findDetailedByFilters(
                        null,
                        null,
                        conversationId,
                        PageRequest.of(0, recentAuditLimit)
                ).stream()
                .map(this::toAuditEventDto)
                .toList();
        return ChatAdminConversationSupportViewDTO.builder()
                .conversation(toConversationSummary(conversation))
                .recentMessages(recentMessages.stream()
                        .map(message -> toMessageDto(message, reactionsByMessageId.getOrDefault(message.getId(), List.of())))
                        .toList())
                .recentAuditEvents(recentAuditEvents)
                .recentMessagesLimit(recentMessagesLimit)
                .recentAuditLimit(recentAuditLimit)
                .build();
    }

    @Transactional
    public void moderateDeleteMessage(Long conversationId, Long messageId, AppUser currentUser) {
        requireAdmin(currentUser);
        ChatConversation conversation = chatConversationRepository.findDetailedById(conversationId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat conversation not found with id " + conversationId));
        ChatMessage message = chatMessageRepository.findDetailedById(messageId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat message not found with id " + messageId));
        if (!Objects.equals(message.getConversation().getId(), conversation.getId())) {
            throw ServiceErrors.notFound("Chat message not found with id " + messageId);
        }
        if (message.getDeletedAt() != null) {
            return;
        }
        Instant now = Instant.now();
        message.setMessageBody(null);
        message.setImageDataUrl(null);
        message.setAttachmentDataUrl(null);
        message.setAttachmentName(null);
        message.setAttachmentMimeType(null);
        message.setAttachmentSizeBytes(null);
        message.setAttachmentStorageProvider(null);
        message.setAttachmentStorageKey(null);
        message.setDeletedAt(now);
        message.setUpdatedAt(now);
        chatMessageRepository.save(message);
        refreshConversationPreview(conversation);
        chatAuditService.record(ChatAuditEventType.ADMIN_MESSAGE_REMOVED, currentUser, conversation, null, null, null, Map.of(
                "messageId", messageId,
                "senderUserId", message.getSender().getId()
        ));
    }

    private void requireAdmin(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }

    private int normalizeAuditLimit(Integer requestedLimit) {
        int max = Math.max(chatProperties.getModeration().getAuditListMaxLimit(), 1);
        if (requestedLimit == null) {
            return Math.min(50, max);
        }
        return Math.min(Math.max(requestedLimit, 1), max);
    }

    private ChatAuditEventType parseEventType(String requestedEventType) {
        if (requestedEventType == null || requestedEventType.isBlank()) {
            return null;
        }
        try {
            return ChatAuditEventType.valueOf(requestedEventType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw ServiceErrors.badRequest("Unsupported chat audit event type");
        }
    }

    private ChatAuditEventDTO toAuditEventDto(ChatAuditEvent event) {
        return ChatAuditEventDTO.builder()
                .id(event.getId())
                .eventType(event.getEventType().name())
                .userId(event.getUser() == null ? null : event.getUser().getId())
                .username(event.getUser() == null ? null : event.getUser().getUsername())
                .conversationId(event.getConversation() == null ? null : event.getConversation().getId())
                .conversationType(event.getConversation() == null ? null : event.getConversation().getConversationType().name())
                .sessionId(event.getSessionId())
                .remoteAddress(event.getRemoteAddress())
                .userAgent(event.getUserAgent())
                .detailsJson(event.getDetailsJson())
                .createdAt(event.getCreatedAt().toString())
                .build();
    }

    private ChatConversationSummaryDTO toConversationSummary(ChatConversation conversation) {
        return ChatConversationSummaryDTO.builder()
                .conversationId(conversation.getId())
                .conversationType(conversation.getConversationType().name())
                .contextType(conversation.getContextType() == null ? null : conversation.getContextType().name())
                .contextId(conversation.getContextId())
                .title(conversation.getTitle())
                .ownerUserId(conversation.getOwner() == null ? null : conversation.getOwner().getId())
                .participantCount(conversation.getParticipants() == null ? 0 : conversation.getParticipants().size())
                .lastMessageId(conversation.getLastMessageId())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt() == null ? null : conversation.getLastMessageAt().toString())
                .lastMessageHasImage(conversation.isLastMessageHasImage())
                .lastMessageDeleted(conversation.isLastMessageDeleted())
                .build();
    }

    private ChatMessageDTO toMessageDto(ChatMessage message, List<ChatMessageReaction> reactions) {
        String seenAt = message.getSeenAt() == null ? null : message.getSeenAt().toString();
        ResolvedMessageAttachment attachment = resolveMessageAttachment(message);
        return ChatMessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .conversationType(message.getConversation().getConversationType().name())
                .senderUserId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderAvatarDataUrl(message.getSender().getProfileAvatarDataUrl())
                .messageBody(message.getDeletedAt() == null ? message.getMessageBody() : null)
                .imageDataUrl(message.getDeletedAt() == null ? message.getImageDataUrl() : null)
                .attachmentName(attachment.attachmentName())
                .attachmentMimeType(attachment.attachmentMimeType())
                .attachmentStorageProvider(attachment.storageProvider())
                .attachmentStorageKey(attachment.storageKey())
                .attachmentUrl(attachment.url())
                .attachmentUrlExpiresAt(attachment.expiresAt())
                .attachmentSizeBytes(attachment.sizeBytes())
                .replyToMessageId(message.getReplyToMessageId())
                .clientMessageId(message.getClientMessageId())
                .createdAt(message.getCreatedAt().toString())
                .updatedAt(message.getUpdatedAt().toString())
                .editedAt(message.getEditedAt() == null ? null : message.getEditedAt().toString())
                .deletedAt(message.getDeletedAt() == null ? null : message.getDeletedAt().toString())
                .readAt(seenAt)
                .deliveredAt(message.getDeliveredAt() == null ? null : message.getDeliveredAt().toString())
                .seenAt(seenAt)
                .reactions(reactions.stream()
                        .map(reaction -> com.themuffinman.app.chat.dto.ChatMessageReactionDTO.builder()
                                .id(reaction.getId())
                                .userId(reaction.getUser().getId())
                                .username(reaction.getUser().getUsername())
                                .emoji(reaction.getEmoji())
                                .createdAt(reaction.getCreatedAt().toString())
                                .ownReaction(false)
                                .build())
                        .toList())
                .edited(message.getEditedAt() != null)
                .deleted(message.getDeletedAt() != null)
                .ownMessage(false)
                .build();
    }

    private ResolvedMessageAttachment resolveMessageAttachment(ChatMessage message) {
        if (message.getDeletedAt() != null) {
            return ResolvedMessageAttachment.empty();
        }
        if (message.getAttachmentStorageKey() != null) {
            ObjectStorageAccess access = objectStorageService.resolve(message.getAttachmentStorageKey());
            return new ResolvedMessageAttachment(
                    message.getAttachmentName(),
                    message.getAttachmentMimeType(),
                    message.getAttachmentSizeBytes(),
                    access.provider(),
                    access.storageKey(),
                    access.url(),
                    access.expiresAt() == null ? null : access.expiresAt().toString()
            );
        }
        if (message.getAttachmentDataUrl() != null) {
            return new ResolvedMessageAttachment(
                    message.getAttachmentName(),
                    message.getAttachmentMimeType(),
                    message.getAttachmentSizeBytes(),
                    "INLINE",
                    null,
                    message.getAttachmentDataUrl(),
                    null
            );
        }
        return ResolvedMessageAttachment.empty();
    }

    private Map<Long, List<ChatMessageReaction>> groupReactionsByMessageId(List<ChatMessage> messages) {
        List<Long> messageIds = messages.stream().map(ChatMessage::getId).filter(Objects::nonNull).toList();
        if (messageIds.isEmpty()) {
            return Map.of();
        }
        return chatMessageReactionRepository.findDetailedByMessageIdIn(messageIds).stream()
                .collect(Collectors.groupingBy(reaction -> reaction.getMessage().getId()));
    }

    private void refreshConversationPreview(ChatConversation conversation) {
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestDetailedByConversationId(conversation.getId(), PageRequest.of(0, 1));
        if (latestMessages.isEmpty()) {
            conversation.setLastMessageAt(null);
            conversation.setLastMessageId(null);
            conversation.setLastMessageSender(null);
            conversation.setLastMessagePreview(null);
            conversation.setLastMessageHasImage(false);
            conversation.setLastMessageDeleted(false);
            chatConversationRepository.save(conversation);
            return;
        }

        ChatMessage latestMessage = latestMessages.getFirst();
        conversation.setLastMessageAt(latestMessage.getCreatedAt());
        conversation.setLastMessageId(latestMessage.getId());
        conversation.setLastMessageSender(latestMessage.getSender());
        conversation.setLastMessagePreview(buildPreview(latestMessage));
        conversation.setLastMessageHasImage(latestMessage.getDeletedAt() == null && latestMessage.getImageDataUrl() != null);
        conversation.setLastMessageDeleted(latestMessage.getDeletedAt() != null);
        chatConversationRepository.save(conversation);
    }

    private String buildPreview(ChatMessage message) {
        if (message.getDeletedAt() != null) {
            return chatProperties.getMessages().getDeletedMessagePlaceholder();
        }
        if (message.getMessageBody() != null && !message.getMessageBody().isBlank()) {
            return message.getMessageBody().length() <= 140
                    ? message.getMessageBody()
                    : message.getMessageBody().substring(0, 139) + "…";
        }
        if (message.getAttachmentName() != null && !message.getAttachmentName().isBlank()) {
            return "Attachment: " + message.getAttachmentName();
        }
        return "Photo";
    }

    private record ResolvedMessageAttachment(
            String attachmentName,
            String attachmentMimeType,
            Integer sizeBytes,
            String storageProvider,
            String storageKey,
            String url,
            String expiresAt
    ) {
        private static ResolvedMessageAttachment empty() {
            return new ResolvedMessageAttachment(null, null, null, null, null, null, null);
        }
    }
}
