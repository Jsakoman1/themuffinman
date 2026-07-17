package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.dto.OnboardingProgressRequestDTO;
import com.themuffinman.app.identity.dto.OnboardingProgressResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.OnboardingProgress;
import com.themuffinman.app.identity.repository.OnboardingProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class OnboardingProgressService {
    private static final List<String> STEPS = List.of("WELCOME", "PROFILE", "PRIVACY", "DISCOVER", "DONE");
    private final OnboardingProgressRepository repository;
    public OnboardingProgressResponseDTO getMine(AppUser user) { return repository.findByUserId(user.getId()).map(this::toDto).orElseGet(() -> OnboardingProgressResponseDTO.builder().currentStep("WELCOME").build()); }
    @Transactional
    public OnboardingProgressResponseDTO updateMine(OnboardingProgressRequestDTO request, AppUser user) {
        if (request == null || request.getCurrentStep() == null) throw ServiceErrors.badRequest("Onboarding step is required");
        String step = request.getCurrentStep().trim().toUpperCase(); if (!STEPS.contains(step)) throw ServiceErrors.badRequest("Unsupported onboarding step");
        OnboardingProgress progress = repository.findByUserId(user.getId()).orElseGet(() -> { OnboardingProgress created = new OnboardingProgress(); created.setUser(user); return created; });
        progress.setCurrentStep(step); progress.setSkipped(request.isSkipped()); progress.setCompleted(request.isCompleted() || "DONE".equals(step));
        return toDto(repository.save(progress));
    }
    @Transactional public OnboardingProgressResponseDTO reset(AppUser user) { OnboardingProgress progress = repository.findByUserId(user.getId()).orElseGet(() -> { OnboardingProgress created = new OnboardingProgress(); created.setUser(user); return created; }); progress.setCurrentStep("WELCOME"); progress.setSkipped(false); progress.setCompleted(false); return toDto(repository.save(progress)); }
    private OnboardingProgressResponseDTO toDto(OnboardingProgress progress) { return OnboardingProgressResponseDTO.builder().id(progress.getId()).currentStep(progress.getCurrentStep()).skipped(progress.isSkipped()).completed(progress.isCompleted()).updatedAt(progress.getUpdatedAt()).build(); }
}
