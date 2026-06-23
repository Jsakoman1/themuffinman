package com.sidequest.sidequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDetailResponseDTO {
    private QuestResponseDTO summary;
    private QuestDetailSectionsDTO sections;
    private QuestResponseDTO quest;
    private QuestApplicationResponseDTO myApplication;
    private QuestApplicationsViewDTO applicationsView;
}
