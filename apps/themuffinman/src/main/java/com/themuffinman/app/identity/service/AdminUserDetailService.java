package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.dto.AdminUserDetailDTO;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.social.service.CircleRelationshipReadService;
import com.themuffinman.app.vision.dto.VisionOptionsDTO;
import com.themuffinman.app.vision.service.VisionOptionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserDetailService {
    private final AppUserLookupService appUserLookupService;
    private final AppUserReadService appUserReadService;
    private final IdentityUserSummaryAssembler identityUserSummaryAssembler;
    private final CircleReadService circleReadService;
    private final CircleRelationshipReadService circleRelationshipReadService;
    private final VisionOptionsService visionOptionsService;

    public AdminUserDetailDTO getDetail(Long userId, AppUser currentUser) {
        validateAdmin(currentUser);

        AppUser targetUser = appUserLookupService.requireById(userId);
        AppUserResponseDTO user = identityUserSummaryAssembler.buildProfileSummary(
                targetUser,
                appUserReadService.countQuestsByCreatorId(targetUser.getId()),
                appUserReadService.getOpenQuestsByCreatorId(targetUser.getId())
        );
        VisionOptionsDTO options = visionOptionsService.getOptions(targetUser);

        return AdminUserDetailDTO.builder()
                .user(user)
                .appUserRoles(options.getAppUserRoles())
                .locationModes(options.getLocationModes())
                .exactLocationVisibilityScopes(options.getExactLocationVisibilityScopes())
                .circles(circleReadService.getCirclesForUserAsAdmin(targetUser.getId(), currentUser))
                .contacts(circleReadService.getConnectionsForUserAsAdmin(targetUser.getId(), currentUser))
                .build();
    }

    private void validateAdmin(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }
}
