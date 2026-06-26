package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
