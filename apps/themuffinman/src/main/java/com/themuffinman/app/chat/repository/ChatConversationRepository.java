package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByLeftParticipantIdAndRightParticipantId(Long leftParticipantId, Long rightParticipantId);

    @Query("""
            select conversation from ChatConversation conversation
            join fetch conversation.leftParticipant
            join fetch conversation.rightParticipant
            left join fetch conversation.lastMessageSender
            where conversation.id = :conversationId
            """)
    Optional<ChatConversation> findDetailedById(Long conversationId);

    @Query("""
            select conversation from ChatConversation conversation
            join fetch conversation.leftParticipant
            join fetch conversation.rightParticipant
            left join fetch conversation.lastMessageSender
            where conversation.leftParticipant.id = :userId
               or conversation.rightParticipant.id = :userId
            order by coalesce(conversation.lastMessageAt, conversation.createdAt) desc
            """)
    List<ChatConversation> findDetailedByParticipantId(Long userId);
}
