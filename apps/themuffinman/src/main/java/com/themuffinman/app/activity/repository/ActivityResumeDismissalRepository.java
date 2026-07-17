package com.themuffinman.app.activity.repository;

import com.themuffinman.app.activity.model.ActivityResumeDismissal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityResumeDismissalRepository extends JpaRepository<ActivityResumeDismissal, Long> { boolean existsByUserIdAndResumeKey(Long userId, String resumeKey); }
