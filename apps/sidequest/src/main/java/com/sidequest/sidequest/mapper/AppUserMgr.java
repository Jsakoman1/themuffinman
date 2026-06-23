package com.sidequest.sidequest.mapper;

import com.sidequest.sidequest.dto.AppUserResponseDTO;
import com.sidequest.sidequest.dto.QuestResponseDTO;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.service.RichTextInputValidator;

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
