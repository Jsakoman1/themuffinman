package com.themuffinman.app.vision.dto;

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
public class QuestApplicationsViewDTO {
    @Nullable
    private QuestApplicationResponseDTO featuredApplication;
    private List<QuestApplicationResponseDTO> approvedApplications;
    private List<QuestApplicationResponseDTO> visibleApplications;
    private int pendingApplicationCount;
    @Nullable
    private Long oldestPendingApplicationId;
    private int hiddenApplicationsCount;
    @Nullable
    private Long selectedApplicationId;
    private boolean canRevealHiddenApplications;
    private boolean showingAllApplications;
    private String revealLabel;
}
