package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDetailResponseDTO {
    private QuestResponseDTO summary;
    private QuestDetailSectionsDTO sections;
    private QuestResponseDTO quest;
    @Nullable
    private QuestApplicationResponseDTO myApplication;
    @Nullable
    private QuestApplicationsViewDTO applicationsView;
    private List<QuestDetailRailItemDTO> propertyRail;
    private List<QuestDetailRailItemDTO> activityRail;
}
