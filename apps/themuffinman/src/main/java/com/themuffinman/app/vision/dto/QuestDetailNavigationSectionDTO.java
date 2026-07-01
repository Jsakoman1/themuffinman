package com.themuffinman.app.vision.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestDetailNavigationSectionDTO {
    private NavigationTargetDTO listNavigation;
}
