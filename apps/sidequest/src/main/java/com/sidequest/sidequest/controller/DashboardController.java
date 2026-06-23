package com.sidequest.sidequest.controller;

import com.sidequest.sidequest.dto.DashboardResponseDTO;
import com.sidequest.sidequest.dto.DashboardSummaryDTO;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
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
}
