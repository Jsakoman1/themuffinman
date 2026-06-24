package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDetailSectionsDTO {
    private QuestDetailNavigationSectionDTO navigation;
    @Nullable
    private QuestApplicationResponseDTO myApplication;
    @Nullable
    private QuestApplicationsViewDTO applicationsView;
    private QuestDetailReviewSectionDTO review;
    private QuestDetailExecutionSectionDTO execution;
    private QuestDetailTermChangeSectionDTO termChange;
    private QuestDetailManagementSectionDTO management;
}
