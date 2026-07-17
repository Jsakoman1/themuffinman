package com.themuffinman.app.identity.repository;

import com.themuffinman.app.identity.model.OnboardingProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnboardingProgressRepository extends JpaRepository<OnboardingProgress, Long> { Optional<OnboardingProgress> findByUserId(Long userId); }
