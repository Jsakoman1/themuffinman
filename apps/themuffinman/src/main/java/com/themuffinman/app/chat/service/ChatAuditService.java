package com.themuffinman.app.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.chat.model.ChatAuditEvent;
import com.themuffinman.app.chat.model.ChatAuditEventType;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.repository.ChatAuditEventRepository;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatAuditService {

    private final ChatAuditEventRepository chatAuditEventRepository;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public void record(
            ChatAuditEventType eventType,
            AppUser user,
            ChatConversation conversation,
            String sessionId,
            String remoteAddress,
            String userAgent,
            Map<String, Object> details
    ) {
        ChatAuditEvent event = new ChatAuditEvent();
        event.setEventType(eventType);
        event.setUser(user);
        event.setConversation(conversation);
        event.setSessionId(normalize(sessionId));
        event.setRemoteAddress(normalize(remoteAddress));
        event.setUserAgent(normalize(userAgent));
        event.setDetailsJson(serialize(details));
        event.setCreatedAt(Instant.now());
        chatAuditEventRepository.save(event);
    }

    private String serialize(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":true}";
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
