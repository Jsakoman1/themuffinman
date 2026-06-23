package com.sidequest.sidequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDetailSectionsDTO {
    private QuestApplicationResponseDTO myApplication;
    private QuestApplicationsViewDTO applicationsView;
}
