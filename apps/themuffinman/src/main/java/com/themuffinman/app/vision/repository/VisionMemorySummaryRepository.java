package com.themuffinman.app.vision.repository;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionMemorySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisionMemorySummaryRepository extends JpaRepository<VisionMemorySummary, Long> {
    Optional<VisionMemorySummary> findTopByUserOrderByCreatedAtDesc(AppUser user);
    List<VisionMemorySummary> findTop5ByUserOrderByCreatedAtDesc(AppUser user);
}
