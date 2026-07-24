package com.themuffinman.app.vision.repository;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionUserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface VisionUserPreferenceRepository extends JpaRepository<VisionUserPreference, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VisionUserPreference> findByUserAndPreferenceKey(AppUser user, String preferenceKey);
    List<VisionUserPreference> findByUser(AppUser user);
}
