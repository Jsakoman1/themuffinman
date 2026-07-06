package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.DashboardResponseDTO;
import com.themuffinman.app.vision.dto.DashboardSummaryDTO;
import com.themuffinman.app.vision.dto.DashboardVoiceConfigDTO;
import com.themuffinman.app.workmarket.service.WorkmarketDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VisionDashboardFacadeService {

    private final WorkmarketDashboardService dashboardService;

    public DashboardResponseDTO getMyDashboard(AppUser currentUser) {
        return dashboardService.getMyDashboard(currentUser);
    }

    public DashboardSummaryDTO getMySummary(AppUser currentUser) {
        return dashboardService.getMySummary(currentUser);
    }

    public DashboardVoiceConfigDTO getMyVoiceConfig(AppUser currentUser) {
        return dashboardService.getMyVoiceConfig(currentUser);
    }
}
