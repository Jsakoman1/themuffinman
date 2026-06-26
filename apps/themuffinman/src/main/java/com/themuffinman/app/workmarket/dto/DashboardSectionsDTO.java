package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSectionsDTO {
    private List<QuestResponseDTO> recentMyQuests;
    private List<QuestApplicationResponseDTO> recentMyApplications;
    private List<QuestApplicationResponseDTO> activeWorkApplications;
    private List<QuestResponseDTO> incomingWorkQuests;
    private List<QuestApplicationResponseDTO> outgoingWorkApplications;
    private List<QuestResponseDTO> visibleMyQuests;
    private List<QuestApplicationResponseDTO> visibleMyApplications;
    private List<DashboardQuestGroupDTO> myQuestGroups;
    private List<DashboardApplicationGroupDTO> myApplicationGroups;
    private List<CircleRequestResponseDTO> recentIncomingCircleRequests;
    private DashboardOpenWorkSectionDTO openWork;
    private DashboardPlannerSectionDTO planner;
    private DashboardNotificationsSectionDTO notifications;
}
