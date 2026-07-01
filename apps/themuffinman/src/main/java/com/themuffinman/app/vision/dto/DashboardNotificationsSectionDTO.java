package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardNotificationsSectionDTO {
    private List<DashboardNotificationItemDTO> unreadItems;
    private List<DashboardNotificationItemDTO> recentItems;
}
