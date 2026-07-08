package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatMessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMessageReactionRepository extends JpaRepository<ChatMessageReaction, Long> {

    @Query("""
            select reaction from ChatMessageReaction reaction
            join fetch reaction.user
            where reaction.message.id in :messageIds
            order by reaction.createdAt asc, reaction.id asc
            """)
    List<ChatMessageReaction> findDetailedByMessageIdIn(List<Long> messageIds);

    @Query("""
            select reaction from ChatMessageReaction reaction
            join fetch reaction.user
            where reaction.message.id = :messageId
              and reaction.user.id = :userId
              and reaction.emoji = :emoji
            """)
    Optional<ChatMessageReaction> findDetailedByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);
}
