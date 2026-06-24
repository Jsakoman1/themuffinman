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
public class WorkmarketOptionsDTO {
    private List<AppUserRoleOptionDTO> appUserRoles;
    private List<QuestStatusFilterOptionDTO> questStatusFilters;
    private List<QuestApplicationStatusFilterOptionDTO> questApplicationStatusFilters;
    private List<QuestStatusOptionDTO> questStatuses;
    private List<QuestAudienceOptionDTO> questAudiences;
    private List<QuestAudienceFilterOptionDTO> questAudienceFilters;
    private List<QuestSortOptionDTO> questSortOptions;
}
