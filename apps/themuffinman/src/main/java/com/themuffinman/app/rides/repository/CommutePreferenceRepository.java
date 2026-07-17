package com.themuffinman.app.rides.repository;

import com.themuffinman.app.rides.model.CommutePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommutePreferenceRepository extends JpaRepository<CommutePreference, Long> {
    Optional<CommutePreference> findByUserId(Long userId);
}
