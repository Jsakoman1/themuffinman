package com.themuffinman.app.identity.repository;

import com.themuffinman.app.identity.model.WorkspaceRailPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkspaceRailPreferenceRepository extends JpaRepository<WorkspaceRailPreference, Long> {
    Optional<WorkspaceRailPreference> findByOwnerId(Long ownerId);
}
