package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.model.ChatConversationContextType;
import com.themuffinman.app.chat.model.ChatConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByLeftParticipantIdAndRightParticipantIdAndConversationType(
            Long leftParticipantId,
            Long rightParticipantId,
            ChatConversationType conversationType
    );

    @Query("""
            select distinct conversation from ChatConversation conversation
            left join fetch conversation.leftParticipant
            left join fetch conversation.rightParticipant
            left join fetch conversation.owner
            left join fetch conversation.createdBy
            left join fetch conversation.lastMessageSender
            left join fetch conversation.participants participant
            left join fetch participant.user
            where conversation.id = :conversationId
            """)
    Optional<ChatConversation> findDetailedById(Long conversationId);

    @Query("""
            select distinct conversation from ChatConversation conversation
            left join fetch conversation.leftParticipant
            left join fetch conversation.rightParticipant
            left join fetch conversation.owner
            left join fetch conversation.createdBy
            left join fetch conversation.lastMessageSender
            left join fetch conversation.participants participant
            left join fetch participant.user
            where exists (
                select 1
                from ChatConversationParticipant membership
                where membership.conversation.id = conversation.id
                  and membership.user.id = :userId
            )
            """)
    List<ChatConversation> findDetailedByParticipantId(Long userId);

    Optional<ChatConversation> findByContextTypeAndContextId(ChatConversationContextType contextType, Long contextId);

    @Query("""
            select distinct conversation from ChatConversation conversation
            left join fetch conversation.leftParticipant
            left join fetch conversation.rightParticipant
            left join fetch conversation.owner
            left join fetch conversation.createdBy
            left join fetch conversation.lastMessageSender
            left join fetch conversation.participants participant
            left join fetch participant.user
            where conversation.contextType = :contextType
              and conversation.contextId = :contextId
            """)
    Optional<ChatConversation> findDetailedByContextTypeAndContextId(ChatConversationContextType contextType, Long contextId);
}
