package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.location.dto.ExactLocationVisibilityScopeOptionDTO;
import com.themuffinman.app.location.dto.LocationModeOptionDTO;
import com.themuffinman.app.location.dto.QuestLocationVisibilityOptionDTO;
import com.themuffinman.app.workmarket.dto.AppUserRoleOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationStatusFilterOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestAudienceFilterOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestAudienceOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestSearchDefaultsDTO;
import com.themuffinman.app.workmarket.dto.QuestSortOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestStatusFilterOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestStatusOptionDTO;
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
    private QuestSearchDefaultsDTO questSearchDefaults;
    private List<LocationModeOptionDTO> locationModes;
    private List<ExactLocationVisibilityScopeOptionDTO> exactLocationVisibilityScopes;
    private List<QuestLocationVisibilityOptionDTO> questLocationVisibilities;
}
