package com.themuffinman.app.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import com.themuffinman.app.chat.dto.ChatSocketEventDTO;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.config.ChatProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatRealtimeService {

    private static final String WORKSPACE_UPDATED = "chat.workspace.updated";
    private static final String NEWS_UPDATED = "news.updated";
    private static final String TYPING_UPDATED = "chat.typing.updated";

    private final ChatPresenceService chatPresenceService;
    private final CircleRequestRepository circleRequestRepository;
    private final ChatProperties chatProperties;

    private final Map<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Instant>> typingExpiryByConversationId = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public void register(AppUser currentUser, WebSocketSession session) {
        sessionsByUserId.computeIfAbsent(currentUser.getId(), ignored -> new CopyOnWriteArraySet<>()).add(session);
        chatPresenceService.markActive(currentUser);
        notifyPresenceChanged(currentUser, "presence_online");
    }

    public void unregister(AppUser currentUser, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(currentUser.getId());
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByUserId.remove(currentUser.getId());
                chatPresenceService.markInactive(currentUser);
                notifyPresenceChanged(currentUser, "presence_offline");
            }
        }
    }

    public void refreshPresence(AppUser currentUser) {
        chatPresenceService.markActive(currentUser);
    }

    public void notifyMessageCreated(
            ChatConversation conversation,
            Long actorUserId,
            Map<Long, ChatConversationSummaryDTO> summariesByUserId,
            Map<Long, ChatMessageDTO> messagesByUserId
    ) {
        notifyConversationEvent(conversation, actorUserId, "message_created", summariesByUserId, messagesByUserId, null, null);
    }

    public void notifyMessageUpdated(
            ChatConversation conversation,
            Long actorUserId,
            Map<Long, ChatConversationSummaryDTO> summariesByUserId,
            Map<Long, ChatMessageDTO> messagesByUserId
    ) {
        notifyConversationEvent(conversation, actorUserId, "message_updated", summariesByUserId, messagesByUserId, null, null);
    }

    public void notifyMessageDeleted(
            ChatConversation conversation,
            Long actorUserId,
            Map<Long, ChatConversationSummaryDTO> summariesByUserId,
            Map<Long, ChatMessageDTO> messagesByUserId
    ) {
        notifyConversationEvent(conversation, actorUserId, "message_deleted", summariesByUserId, messagesByUserId, null, null);
    }

    public void notifyConversationStateUpdated(
            ChatConversation conversation,
            Long actorUserId,
            Map<Long, ChatConversationSummaryDTO> summariesByUserId
    ) {
        notifyConversationEvent(conversation, actorUserId, "conversation_state_updated", summariesByUserId, Map.of(), null, null);
    }

    public void notifyConversationDelivered(
            ChatConversation conversation,
            Long actorUserId,
            Map<Long, ChatConversationSummaryDTO> summariesByUserId,
            Long deliveredUpToMessageId
    ) {
        notifyConversationEvent(conversation, actorUserId, "conversation_delivered", summariesByUserId, Map.of(), deliveredUpToMessageId, null);
    }

    public void notifyConversationSeen(
            ChatConversation conversation,
            Long actorUserId,
            Map<Long, ChatConversationSummaryDTO> summariesByUserId,
            Long seenUpToMessageId
    ) {
        notifyConversationEvent(conversation, actorUserId, "conversation_seen", summariesByUserId, Map.of(), null, seenUpToMessageId);
    }

    public void notifyWorkspaceChanged(Long userId, Long conversationId, Long actorUserId, String reason) {
        sendToUser(userId, ChatSocketEventDTO.builder()
                .type(WORKSPACE_UPDATED)
                .conversationId(conversationId)
                .actorUserId(actorUserId)
                .reason(reason)
                .build());
    }

    public void notifyNewsUpdated(Long userId, Long actorUserId, long unreadNewsCount, String reason) {
        sendToUser(userId, ChatSocketEventDTO.builder()
                .type(NEWS_UPDATED)
                .actorUserId(actorUserId)
                .reason(reason)
                .unreadNewsCount(unreadNewsCount)
                .build());
    }

    public void notifyTypingChanged(ChatConversation conversation, Long actorUserId, boolean typing) {
        Map<Long, Instant> typingByUserId = typingExpiryByConversationId.computeIfAbsent(conversation.getId(), ignored -> new ConcurrentHashMap<>());
        if (typing) {
            typingByUserId.put(actorUserId, Instant.now().plusSeconds(chatProperties.getTyping().getTimeoutSeconds()));
        } else {
            typingByUserId.remove(actorUserId);
        }
        conversation.getParticipants().stream()
                .filter(participant -> !participant.getUser().getId().equals(actorUserId))
                .forEach(participant -> sendToUser(participant.getUser().getId(), ChatSocketEventDTO.builder()
                        .type(TYPING_UPDATED)
                        .conversationId(conversation.getId())
                        .actorUserId(actorUserId)
                        .reason("typing_changed")
                        .typing(typing)
                        .build()));
    }

    public List<Long> getActiveTypingUserIds(Long conversationId, Long excludedUserId, int timeoutSeconds) {
        Map<Long, Instant> typingByUserId = typingExpiryByConversationId.get(conversationId);
        if (typingByUserId == null || typingByUserId.isEmpty()) {
            return List.of();
        }
        Instant fallbackCutoff = Instant.now().minusSeconds(Math.max(timeoutSeconds, 1));
        typingByUserId.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isBefore(fallbackCutoff));
        return typingByUserId.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().isAfter(Instant.now()))
                .map(Map.Entry::getKey)
                .filter(userId -> excludedUserId == null || !excludedUserId.equals(userId))
                .sorted()
                .toList();
    }

    private void notifyConversationEvent(
            ChatConversation conversation,
            Long actorUserId,
            String reason,
            Map<Long, ChatConversationSummaryDTO> summariesByUserId,
            Map<Long, ChatMessageDTO> messagesByUserId,
            Long deliveredUpToMessageId,
            Long seenUpToMessageId
    ) {
        conversation.getParticipants().forEach(participant -> sendToUser(participant.getUser().getId(), ChatSocketEventDTO.builder()
                .type(WORKSPACE_UPDATED)
                .conversationId(conversation.getId())
                .actorUserId(actorUserId)
                .reason(reason)
                .conversation(summariesByUserId.get(participant.getUser().getId()))
                .message(messagesByUserId.get(participant.getUser().getId()))
                .readUpToMessageId(seenUpToMessageId)
                .deliveredUpToMessageId(deliveredUpToMessageId)
                .seenUpToMessageId(seenUpToMessageId)
                .build()));
    }

    private void notifyPresenceChanged(AppUser actor, String reason) {
        notifyWorkspaceChanged(actor.getId(), null, actor.getId(), reason);
        List<CircleRequest> acceptedRelations = circleRequestRepository.findAcceptedByUserId(actor.getId());
        for (CircleRequest relation : acceptedRelations) {
            AppUser counterpart = relation.getRequester().getId().equals(actor.getId())
                    ? relation.getRecipient()
                    : relation.getRequester();
            notifyWorkspaceChanged(counterpart.getId(), null, actor.getId(), reason);
        }
    }

    private void sendToUser(Long userId, ChatSocketEventDTO event) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String payload = serialize(event);
        if (payload == null) {
            return;
        }

        TextMessage textMessage = new TextMessage(payload);
        sessions.removeIf(session -> !sendMessage(session, textMessage));
        if (sessions.isEmpty()) {
            sessionsByUserId.remove(userId);
        }
    }

    private String serialize(ChatSocketEventDTO event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private boolean sendMessage(WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) {
            return false;
        }

        try {
            session.sendMessage(message);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }
}
