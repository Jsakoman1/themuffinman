package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    interface UnreadCountRow {
        Long getConversationId();

        long getUnreadCount();
    }

    @Query("""
            select message from ChatMessage message
            join fetch message.sender
            where message.conversation.id = :conversationId
            order by message.createdAt asc
            """)
    List<ChatMessage> findDetailedByConversationId(Long conversationId);

    @Query("""
            select message.conversation.id as conversationId, count(message) as unreadCount
            from ChatMessage message
            where message.conversation.id in :conversationIds
              and message.sender.id <> :userId
              and message.readAt is null
            group by message.conversation.id
            """)
    List<UnreadCountRow> findUnreadCountsByConversationIds(List<Long> conversationIds, Long userId);

    @Query("""
            select message from ChatMessage message
            where message.conversation.id = :conversationId
              and message.sender.id <> :userId
              and message.readAt is null
            """)
    List<ChatMessage> findUnreadIncomingByConversationId(Long conversationId, Long userId);

    @Query("""
            select distinct message.conversation.id from ChatMessage message
            where message.imageDataUrl is not null
              and message.createdAt < :cutoff
            """)
    List<Long> findConversationIdsWithExpiredImages(@Param("cutoff") Instant cutoff);

    @Query("""
            select distinct message.conversation.id from ChatMessage message
            where message.createdAt < :cutoff
            """)
    List<Long> findConversationIdsWithExpiredMessages(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query("""
            update ChatMessage message
            set message.imageDataUrl = null,
                message.messageBody = case
                    when message.messageBody is null then :placeholder
                    else message.messageBody
                end
            where message.imageDataUrl is not null
              and message.createdAt < :cutoff
            """)
    int redactExpiredImages(@Param("cutoff") Instant cutoff, @Param("placeholder") String placeholder);

    @Modifying
    @Query("""
            delete from ChatMessage message
            where message.createdAt < :cutoff
            """)
    int deleteByCreatedAtBefore(@Param("cutoff") Instant cutoff);

    @Query("""
            select message from ChatMessage message
            join fetch message.sender
            where message.conversation.id = :conversationId
            order by message.createdAt desc
            """)
    List<ChatMessage> findLatestDetailedByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);
}
