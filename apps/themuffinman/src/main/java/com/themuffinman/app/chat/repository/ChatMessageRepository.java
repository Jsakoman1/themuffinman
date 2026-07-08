package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
            select message from ChatMessage message
            join fetch message.sender
            where message.conversation.id = :conversationId
              and (:beforeMessageId is null or message.id < :beforeMessageId)
            order by message.id desc
            """)
    List<ChatMessage> findDetailedPageByConversationId(Long conversationId, Long beforeMessageId, Pageable pageable);

    @Query("""
            select message from ChatMessage message
            join fetch message.sender
            where message.id = :messageId
            """)
    Optional<ChatMessage> findDetailedById(Long messageId);

    @Query("""
            select message from ChatMessage message
            join fetch message.sender
            where message.attachmentStorageKey = :storageKey
            """)
    Optional<ChatMessage> findDetailedByAttachmentStorageKey(String storageKey);

    @Query("""
            select message from ChatMessage message
            join fetch message.sender
            where message.conversation.id = :conversationId
              and message.sender.id = :senderUserId
              and message.clientMessageId = :clientMessageId
            """)
    Optional<ChatMessage> findByConversationIdAndSenderIdAndClientMessageId(Long conversationId, Long senderUserId, String clientMessageId);

    @Query("""
            select message.conversation.id as conversationId, count(message) as unreadCount
            from ChatMessage message
            where message.conversation.id in :conversationIds
              and message.sender.id <> :userId
              and message.seenAt is null
            group by message.conversation.id
            """)
    List<UnreadCountRow> findUnreadCountsByConversationIds(List<Long> conversationIds, Long userId);

    @Query("""
            select message from ChatMessage message
            where message.conversation.id = :conversationId
              and message.sender.id <> :userId
              and message.seenAt is null
            """)
    List<ChatMessage> findUnreadIncomingByConversationId(Long conversationId, Long userId);

    @Query("""
            select message from ChatMessage message
            where message.conversation.id = :conversationId
              and message.sender.id <> :userId
              and message.seenAt is null
              and (:upToMessageId is null or message.id <= :upToMessageId)
            """)
    List<ChatMessage> findUnreadIncomingByConversationIdUpToMessageId(Long conversationId, Long userId, Long upToMessageId);

    @Query("""
            select message from ChatMessage message
            where message.conversation.id = :conversationId
              and message.sender.id <> :userId
              and message.deliveredAt is null
              and (:upToMessageId is null or message.id <= :upToMessageId)
            """)
    List<ChatMessage> findUndeliveredIncomingByConversationIdUpToMessageId(Long conversationId, Long userId, Long upToMessageId);

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

    @Query("""
            select message from ChatMessage message
            join fetch message.sender
            where message.conversation.id = :conversationId
              and (:afterMessageId is null or message.id > :afterMessageId)
            order by message.id asc
            """)
    List<ChatMessage> findDetailedSinceMessageIdByConversationId(@Param("conversationId") Long conversationId,
                                                                 @Param("afterMessageId") Long afterMessageId,
                                                                 Pageable pageable);
}
