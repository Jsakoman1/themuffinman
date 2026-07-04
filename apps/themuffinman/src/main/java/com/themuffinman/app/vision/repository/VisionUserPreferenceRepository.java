package com.themuffinman.app.vision.repository;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionUserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisionUserPreferenceRepository extends JpaRepository<VisionUserPreference, Long> {
    Optional<VisionUserPreference> findByUserAndPreferenceKey(AppUser user, String preferenceKey);
    List<VisionUserPreference> findByUser(AppUser user);
}
