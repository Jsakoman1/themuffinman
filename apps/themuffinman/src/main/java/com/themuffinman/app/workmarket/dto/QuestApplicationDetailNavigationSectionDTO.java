package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestApplicationDetailNavigationSectionDTO {
    private boolean canOpenQuest;
    private Long questId;
    private NavigationTargetDTO questNavigation;
}
