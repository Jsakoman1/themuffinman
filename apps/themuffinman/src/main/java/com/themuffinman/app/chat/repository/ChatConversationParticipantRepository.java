package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationParticipantRepository extends JpaRepository<ChatConversationParticipant, Long> {

    Optional<ChatConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);

    List<ChatConversationParticipant> findByConversationId(Long conversationId);
}
