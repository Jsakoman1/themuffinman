package com.themuffinman.app.identity.repository;

import com.themuffinman.app.identity.model.AppearancePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppearancePreferenceRepository extends JpaRepository<AppearancePreference, Long> {
    Optional<AppearancePreference> findByOwnerId(long ownerId);
}
