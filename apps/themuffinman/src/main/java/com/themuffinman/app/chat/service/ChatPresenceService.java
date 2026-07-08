package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.model.ChatPresence;
import com.themuffinman.app.chat.repository.ChatPresenceRepository;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.config.ChatProperties;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {

    private final ChatPresenceRepository chatPresenceRepository;
    private final ChatProperties chatProperties;

    @Transactional
    public void markActive(AppUser currentUser) {
        ChatPresence presence = chatPresenceRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    ChatPresence created = new ChatPresence();
                    created.setUser(currentUser);
                    return created;
                });
        presence.setLastActiveAt(TimeSupport.now());
        chatPresenceRepository.save(presence);
    }

    @Transactional
    public void markInactive(AppUser currentUser) {
        ChatPresence presence = chatPresenceRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    ChatPresence created = new ChatPresence();
                    created.setUser(currentUser);
                    return created;
                });
        presence.setLastActiveAt(TimeSupport.now().minusSeconds(chatProperties.getPresence().getOnlineWindowSeconds() + 5));
        chatPresenceRepository.save(presence);
    }

    public boolean isOnline(ChatPresence presence, Instant now) {
        return presence != null
                && presence.getLastActiveAt() != null
                && !presence.getLastActiveAt().isBefore(now.minusSeconds(chatProperties.getPresence().getOnlineWindowSeconds()));
    }
}
