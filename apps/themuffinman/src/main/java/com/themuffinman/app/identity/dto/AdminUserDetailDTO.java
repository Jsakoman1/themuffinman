package com.themuffinman.app.identity.dto;

import com.themuffinman.app.location.dto.ExactLocationVisibilityScopeOptionDTO;
import com.themuffinman.app.location.dto.LocationModeOptionDTO;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.workmarket.dto.AppUserRoleOptionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailDTO {
    private AppUserResponseDTO user;
    private List<AppUserRoleOptionDTO> appUserRoles;
    private List<LocationModeOptionDTO> locationModes;
    private List<ExactLocationVisibilityScopeOptionDTO> exactLocationVisibilityScopes;
    private List<CircleGroupResponseDTO> circles;
    private List<CircleContactDTO> contacts;
}
