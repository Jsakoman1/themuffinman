package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardNavigationSectionDTO {
    private List<DashboardNavigationItemDTO> tabs;
}
