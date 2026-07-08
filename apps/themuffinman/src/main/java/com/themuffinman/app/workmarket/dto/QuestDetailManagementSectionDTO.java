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
public class QuestDetailManagementSectionDTO {
    private boolean editVisible;
    private boolean deleteVisible;
    private boolean postingSettingsVisible;
    @Nullable
    private String audienceLabel;
    @Nullable
    private String visibleToCirclesLabel;
}
