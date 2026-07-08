package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.DashboardResponseDTO;
import com.themuffinman.app.workmarket.dto.DashboardSummaryDTO;
import com.themuffinman.app.workmarket.dto.DashboardVoiceConfigDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("workmarketDashboardService")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkmarketDashboardService {

    private final WorkmarketDashboardReadService workmarketDashboardReadService;

    public DashboardResponseDTO getMyDashboard(AppUser currentUser) {
        return workmarketDashboardReadService.getMyDashboard(currentUser);
    }

    public DashboardSummaryDTO getMySummary(AppUser currentUser) {
        return workmarketDashboardReadService.getMySummary(currentUser);
    }

    public DashboardVoiceConfigDTO getMyVoiceConfig(AppUser currentUser) {
        return workmarketDashboardReadService.getMyVoiceConfig(currentUser);
    }
}
