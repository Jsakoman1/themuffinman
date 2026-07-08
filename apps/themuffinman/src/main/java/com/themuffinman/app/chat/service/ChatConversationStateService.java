package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatConversationStateRequestDTO;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.model.ChatConversationMemberState;
import com.themuffinman.app.chat.model.ChatMessage;
import com.themuffinman.app.chat.repository.ChatConversationMemberStateRepository;
import com.themuffinman.app.chat.repository.ChatMessageRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatConversationStateService {

    private final ChatConversationMemberStateRepository chatConversationMemberStateRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public Map<Long, ChatConversationMemberState> getStatesByConversationId(List<Long> conversationIds, AppUser currentUser) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return Map.of();
        }
        return chatConversationMemberStateRepository.findByConversationIdInAndUserId(conversationIds, currentUser.getId()).stream()
                .collect(Collectors.toMap(state -> state.getConversation().getId(), state -> state));
    }

    @Transactional
    public ChatConversationMemberState markConversationOpened(ChatConversation conversation, AppUser currentUser, Instant now, boolean clearArchive) {
        ChatConversationMemberState state = getOrCreateState(conversation, currentUser);
        state.setLastOpenedAt(now);
        if (clearArchive) {
            state.setArchivedAt(null);
        }
        return chatConversationMemberStateRepository.save(state);
    }

    @Transactional
    public ChatConversationMemberState updateConversationState(
            ChatConversation conversation,
            AppUser currentUser,
            ChatConversationStateRequestDTO request,
            Instant now
    ) {
        if (request == null || (request.getArchived() == null && request.getMutedUntil() == null)) {
            throw ServiceErrors.badRequest("Conversation state update is required");
        }
        ChatConversationMemberState state = getOrCreateState(conversation, currentUser);
        if (request.getArchived() != null) {
            state.setArchivedAt(Boolean.TRUE.equals(request.getArchived()) ? now : null);
        }
        if (request.getMutedUntil() != null) {
            state.setMutedUntil(parseInstant(request.getMutedUntil()));
        }
        return chatConversationMemberStateRepository.save(state);
    }

    @Transactional
    public Long markDelivered(ChatConversation conversation, AppUser currentUser, Long upToMessageId, Instant now) {
        List<ChatMessage> undeliveredMessages = chatMessageRepository.findUndeliveredIncomingByConversationIdUpToMessageId(
                conversation.getId(),
                currentUser.getId(),
                upToMessageId
        );
        if (undeliveredMessages.isEmpty()) {
            return null;
        }
        undeliveredMessages.forEach(message -> message.setDeliveredAt(now));
        chatMessageRepository.saveAll(undeliveredMessages);
        Long effectiveUpToMessageId = undeliveredMessages.getLast().getId();
        updateReceiptState(conversation, currentUser, now, state -> {
            state.setLastDeliveredMessageId(effectiveUpToMessageId);
            state.setLastDeliveredAt(now);
        });
        return effectiveUpToMessageId;
    }

    @Transactional
    public Long markSeen(ChatConversation conversation, AppUser currentUser, Long upToMessageId, Instant now) {
        List<ChatMessage> unseenMessages = chatMessageRepository.findUnreadIncomingByConversationIdUpToMessageId(
                conversation.getId(),
                currentUser.getId(),
                upToMessageId
        );
        if (unseenMessages.isEmpty()) {
            return null;
        }
        unseenMessages.forEach(message -> {
            if (message.getDeliveredAt() == null) {
                message.setDeliveredAt(now);
            }
            message.setSeenAt(now);
            message.setReadAt(now);
        });
        chatMessageRepository.saveAll(unseenMessages);
        Long effectiveUpToMessageId = unseenMessages.getLast().getId();
        updateReceiptState(conversation, currentUser, now, state -> {
            state.setLastDeliveredMessageId(effectiveUpToMessageId);
            state.setLastDeliveredAt(now);
            state.setLastSeenMessageId(effectiveUpToMessageId);
            state.setLastSeenAt(now);
        });
        return effectiveUpToMessageId;
    }

    public boolean isArchived(ChatConversationMemberState state) {
        return state != null && state.getArchivedAt() != null;
    }

    public boolean isMuted(ChatConversationMemberState state, Instant now) {
        return state != null
                && state.getMutedUntil() != null
                && state.getMutedUntil().isAfter(now);
    }

    private void updateReceiptState(ChatConversation conversation, AppUser currentUser, Instant now, Consumer<ChatConversationMemberState> updater) {
        ChatConversationMemberState state = getOrCreateState(conversation, currentUser);
        state.setLastOpenedAt(now);
        updater.accept(state);
        chatConversationMemberStateRepository.save(state);
    }

    private ChatConversationMemberState getOrCreateState(ChatConversation conversation, AppUser currentUser) {
        return chatConversationMemberStateRepository.findByConversationIdAndUserId(conversation.getId(), currentUser.getId())
                .orElseGet(() -> {
                    ChatConversationMemberState state = new ChatConversationMemberState();
                    state.setConversation(conversation);
                    state.setUser(currentUser);
                    return state;
                });
    }

    private Instant parseInstant(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(normalized);
        } catch (DateTimeParseException exception) {
            throw ServiceErrors.badRequest("Conversation mute timestamp must be a valid ISO-8601 instant");
        }
    }
}
