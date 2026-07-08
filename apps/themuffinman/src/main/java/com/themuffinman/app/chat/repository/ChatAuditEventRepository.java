package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatAuditEvent;
import com.themuffinman.app.chat.model.ChatAuditEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatAuditEventRepository extends JpaRepository<ChatAuditEvent, Long> {

    @Query("""
            select event from ChatAuditEvent event
            left join fetch event.user
            left join fetch event.conversation
            where (:eventType is null or event.eventType = :eventType)
              and (:userId is null or event.user.id = :userId)
              and (:conversationId is null or event.conversation.id = :conversationId)
            order by event.createdAt desc, event.id desc
            """)
    List<ChatAuditEvent> findDetailedByFilters(
            ChatAuditEventType eventType,
            Long userId,
            Long conversationId,
            Pageable pageable
    );
}
