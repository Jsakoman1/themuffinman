package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.DashboardVisionPromptRequestDTO;
import com.themuffinman.app.workmarket.dto.DashboardVisionPromptResponseDTO;

public interface WorkmarketDashboardVisionPromptService {

    DashboardVisionPromptResponseDTO process(DashboardVisionPromptRequestDTO dto, AppUser currentUser);
}
