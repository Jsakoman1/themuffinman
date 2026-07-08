package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatConversationMemberState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationMemberStateRepository extends JpaRepository<ChatConversationMemberState, Long> {

    Optional<ChatConversationMemberState> findByConversationIdAndUserId(Long conversationId, Long userId);

    List<ChatConversationMemberState> findByConversationIdInAndUserId(List<Long> conversationIds, Long userId);
}
