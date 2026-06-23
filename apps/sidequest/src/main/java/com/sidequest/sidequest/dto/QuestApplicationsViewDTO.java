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
public class QuestApplicationsViewDTO {
    private QuestApplicationResponseDTO featuredApplication;
    private List<QuestApplicationResponseDTO> visibleApplications;
    private int hiddenApplicationsCount;
    private Long selectedApplicationId;
    private boolean canRevealHiddenApplications;
    private boolean showingAllApplications;
    private String revealLabel;
}
