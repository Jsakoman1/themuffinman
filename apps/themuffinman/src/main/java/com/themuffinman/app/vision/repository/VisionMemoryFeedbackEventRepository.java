package com.themuffinman.app.vision.repository;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionMemoryFeedbackEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface VisionMemoryFeedbackEventRepository extends JpaRepository<VisionMemoryFeedbackEvent, Long> {
    List<VisionMemoryFeedbackEvent> findTop20ByUserOrderByCreatedAtDesc(AppUser user);
    List<VisionMemoryFeedbackEvent> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(AppUser user, Instant createdAtAfter);
    List<VisionMemoryFeedbackEvent> findByUserOrderByCreatedAtDesc(AppUser user);
}
