package com.themuffinman.app.identity.controller;

import com.themuffinman.app.identity.dto.OnboardingProgressRequestDTO;
import com.themuffinman.app.identity.dto.OnboardingProgressResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.OnboardingProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @RequestMapping("/profile/onboarding")
public class OnboardingProgressController {
    private final OnboardingProgressService service;
    @GetMapping("/me") public OnboardingProgressResponseDTO getMine(@AuthenticationPrincipal AppUser user) { return service.getMine(user); }
    @PutMapping("/me") public OnboardingProgressResponseDTO update(@RequestBody OnboardingProgressRequestDTO request, @AuthenticationPrincipal AppUser user) { return service.updateMine(request, user); }
    @PostMapping("/me/reset") public OnboardingProgressResponseDTO reset(@AuthenticationPrincipal AppUser user) { return service.reset(user); }
}
