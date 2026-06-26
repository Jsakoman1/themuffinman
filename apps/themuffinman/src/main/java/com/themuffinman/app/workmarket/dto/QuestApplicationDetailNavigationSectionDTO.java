package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestApplicationDetailNavigationSectionDTO {
    private boolean canOpenQuest;
    private boolean canOpenPostedBy;
    private Long questId;
    private NavigationTargetDTO questNavigation;
    @Nullable
    private NavigationTargetDTO postedByNavigation;
}
