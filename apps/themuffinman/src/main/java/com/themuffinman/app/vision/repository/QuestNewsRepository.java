package com.themuffinman.app.vision.repository;

import com.themuffinman.app.vision.model.QuestNewsItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuestNewsRepository extends JpaRepository<QuestNewsItem, Long> {
    List<QuestNewsItem> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    Optional<QuestNewsItem> findByIdAndRecipientUserId(Long id, Long recipientUserId);

    long countByRecipientUserIdAndReadAtIsNull(Long recipientUserId);

    @Modifying
    @Query("update QuestNewsItem item set item.readAt = :readAt where item.recipientUserId = :recipientUserId and item.readAt is null")
    int markAllAsRead(@Param("recipientUserId") Long recipientUserId, @Param("readAt") Instant readAt);

    @Modifying
    @Query("delete from QuestNewsItem item where item.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") Instant cutoff);
}
