package com.themuffinman.app.workmarket.controller;

import com.themuffinman.app.workmarket.dto.DashboardResponseDTO;
import com.themuffinman.app.workmarket.dto.DashboardSummaryDTO;
import com.themuffinman.app.workmarket.dto.DashboardVoiceConfigDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/me")
    public DashboardResponseDTO getMyDashboard(@AuthenticationPrincipal AppUser currentUser) {
        return dashboardService.getMyDashboard(currentUser);
    }

    @GetMapping("/me/summary")
    public DashboardSummaryDTO getMySummary(@AuthenticationPrincipal AppUser currentUser) {
        return dashboardService.getMySummary(currentUser);
    }

    @GetMapping("/me/voice-config")
    public DashboardVoiceConfigDTO getMyVoiceConfig(@AuthenticationPrincipal AppUser currentUser) {
        return dashboardService.getMyVoiceConfig(currentUser);
    }
}
