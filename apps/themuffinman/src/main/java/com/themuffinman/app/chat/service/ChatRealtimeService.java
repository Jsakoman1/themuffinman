package com.themuffinman.app.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.chat.dto.ChatSocketEventDTO;
import com.themuffinman.app.chat.model.ChatConversation;
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

@Service
@RequiredArgsConstructor
public class ChatRealtimeService {

    private static final String WORKSPACE_UPDATED = "chat.workspace.updated";

    private final ChatPresenceService chatPresenceService;
    private final CircleRequestRepository circleRequestRepository;

    private final Map<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();
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

    public void notifyConversationChanged(ChatConversation conversation, Long actorUserId, String reason) {
        notifyWorkspaceChanged(conversation.getLeftParticipant().getId(), conversation.getId(), actorUserId, reason);
        notifyWorkspaceChanged(conversation.getRightParticipant().getId(), conversation.getId(), actorUserId, reason);
    }

    public void notifyWorkspaceChanged(Long userId, Long conversationId, Long actorUserId, String reason) {
        sendToUser(userId, ChatSocketEventDTO.builder()
                .type(WORKSPACE_UPDATED)
                .conversationId(conversationId)
                .actorUserId(actorUserId)
                .reason(reason)
                .build());
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
