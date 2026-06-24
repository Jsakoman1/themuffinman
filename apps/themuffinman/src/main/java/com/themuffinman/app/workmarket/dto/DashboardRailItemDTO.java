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
public class DashboardRailItemDTO {
    private String id;
    private Long questId;
    private String title;
    private String whenLabel;
    private NavigationTargetDTO navigation;
}
