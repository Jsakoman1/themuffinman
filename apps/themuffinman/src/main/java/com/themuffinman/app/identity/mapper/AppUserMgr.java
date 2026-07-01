package com.themuffinman.app.identity.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.location.service.LocationSettingsViewService;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.common.validation.RichTextInputValidator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppUserMgr {
    private final LocationSettingsViewService locationSettingsViewService;

    public AppUserResponseDTO toDto(AppUser appUser) {
        if (appUser == null) {
            return null;
        }

        return AppUserResponseDTO.builder()
                .id(appUser.getId())
                .email(appUser.getEmail())
                .username(appUser.getUsername())
                .resolutionKey("user:" + appUser.getId())
                .resolutionLabel(appUser.getUsername() + " <" + appUser.getEmail() + ">")
                .exactResolutionEligible(true)
                .profileNavigation(NavigationTargetDTO.builder()
                        .type(NavigationTargetType.USER_PROFILE)
                        .entityId(appUser.getId())
                        .build())
                .profileDescription(RichTextInputValidator.sanitize(appUser.getProfileDescription()))
                .profileAvatarDataUrl(appUser.getProfileAvatarDataUrl())
                .locationSettings(locationSettingsViewService.toDto(appUser))
                .createdAt(appUser.getCreatedAt())
                .role(appUser.getRole() == null ? AppUserRole.USER.name() : appUser.getRole().name())
                .build();
    }

    public AppUserResponseDTO withProfileStats(AppUserResponseDTO dto, long openQuestCount, List<QuestResponseDTO> openQuests) {
        if (dto == null) {
            return null;
        }

        dto.setOpenQuestCount(openQuestCount);
        dto.setOpenQuests(openQuests);
        return dto;
    }
}
