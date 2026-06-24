package com.themuffinman.app.identity.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.common.validation.RichTextInputValidator;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AppUserMgr {
    public AppUserResponseDTO toDto(AppUser appUser) {
        if (appUser == null) {
            return null;
        }

        return AppUserResponseDTO.builder()
                .id(appUser.getId())
                .email(appUser.getEmail())
                .username(appUser.getUsername())
                .profileNavigation(NavigationTargetDTO.builder()
                        .type(NavigationTargetType.USER_PROFILE)
                        .entityId(appUser.getId())
                        .build())
                .profileDescription(RichTextInputValidator.sanitize(appUser.getProfileDescription()))
                .profileAvatarDataUrl(appUser.getProfileAvatarDataUrl())
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
