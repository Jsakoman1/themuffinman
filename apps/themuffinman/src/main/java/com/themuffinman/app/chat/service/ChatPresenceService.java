package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.model.ChatPresence;
import com.themuffinman.app.chat.repository.ChatPresenceRepository;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {

    public static final long ONLINE_WINDOW_SECONDS = 120;

    private final ChatPresenceRepository chatPresenceRepository;

    @Transactional
    public void markActive(AppUser currentUser) {
        ChatPresence presence = chatPresenceRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    ChatPresence created = new ChatPresence();
                    created.setUser(currentUser);
                    return created;
                });
        presence.setLastActiveAt(Instant.now());
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
        presence.setLastActiveAt(Instant.now().minusSeconds(ONLINE_WINDOW_SECONDS + 5));
        chatPresenceRepository.save(presence);
    }

    public boolean isOnline(ChatPresence presence, Instant now) {
        return presence != null
                && presence.getLastActiveAt() != null
                && !presence.getLastActiveAt().isBefore(now.minusSeconds(ONLINE_WINDOW_SECONDS));
    }
}
