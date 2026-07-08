package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.workmarket.dto.WorkmarketOptionsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private WorkmarketOptionsDTO options;
    private DashboardSummaryDTO summary;
    private DashboardSectionsDTO sections;
    private List<QuestResponseDTO> quests;
    private List<QuestResponseDTO> myQuests;
    private List<QuestResponseDTO> availableQuests;
    private List<QuestApplicationResponseDTO> myApplications;
    private List<QuestNewsItemResponseDTO> recentNews;
    private List<CircleRequestResponseDTO> incomingCircleRequests;
    private List<CircleGroupResponseDTO> circles;
    private List<AppUserResponseDTO> appUsers;
}
