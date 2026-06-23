package com.sidequest.sidequest.dto;

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
    private DashboardSummaryDTO summary;
    private List<QuestResponseDTO> quests;
    private List<QuestResponseDTO> myQuests;
    private List<QuestResponseDTO> availableQuests;
    private List<QuestApplicationResponseDTO> myApplications;
    private List<QuestNewsItemResponseDTO> recentNews;
    private List<CircleRequestResponseDTO> incomingCircleRequests;
    private List<CircleGroupResponseDTO> circles;
    private List<AppUserResponseDTO> appUsers;
}
