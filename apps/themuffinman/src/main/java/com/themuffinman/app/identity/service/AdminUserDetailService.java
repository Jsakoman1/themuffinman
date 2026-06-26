package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.dto.AdminUserDetailDTO;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.workmarket.dto.WorkmarketOptionsDTO;
import com.themuffinman.app.workmarket.service.WorkmarketOptionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserDetailService {
    private final AppUserLookupService appUserLookupService;
    private final AppUserService appUserService;
    private final AppUserMgr appUserMgr;
    private final CircleService circleService;
    private final WorkmarketOptionsService workmarketOptionsService;

    public AdminUserDetailDTO getDetail(Long userId, AppUser currentUser) {
        validateAdmin(currentUser);

        AppUser targetUser = appUserLookupService.requireById(userId);
        AppUserResponseDTO user = appUserMgr.withProfileStats(
                appUserMgr.toDto(targetUser),
                appUserService.countQuestsByCreatorId(targetUser.getId()),
                appUserService.getOpenQuestsByCreatorId(targetUser.getId())
        );
        WorkmarketOptionsDTO options = workmarketOptionsService.getOptions(targetUser);

        return AdminUserDetailDTO.builder()
                .user(user)
                .appUserRoles(options.getAppUserRoles())
                .locationModes(options.getLocationModes())
                .exactLocationVisibilityScopes(options.getExactLocationVisibilityScopes())
                .circles(circleService.getCirclesForUserAsAdmin(targetUser.getId(), currentUser))
                .contacts(circleService.getConnectionsForUserAsAdmin(targetUser.getId(), currentUser))
                .build();
    }

    private void validateAdmin(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }
}
