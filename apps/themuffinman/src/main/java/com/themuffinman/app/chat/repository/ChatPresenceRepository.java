package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatPresenceRepository extends JpaRepository<ChatPresence, Long> {

    Optional<ChatPresence> findByUserId(Long userId);

    @Query("""
            select presence from ChatPresence presence
            where presence.user.id in :userIds
            """)
    List<ChatPresence> findByUserIds(List<Long> userIds);
}
