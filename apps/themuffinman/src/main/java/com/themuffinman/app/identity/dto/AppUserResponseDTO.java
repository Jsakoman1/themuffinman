package com.themuffinman.app.identity.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.identity.model.ProfileFieldVisibility;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Set;

import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserResponseDTO {
    private Long id;
    private String email;
    private String username;
    private String resolutionKey;
    private String resolutionLabel;
    private boolean exactResolutionEligible;
    private NavigationTargetDTO profileNavigation;
    @Nullable
    private String profileDescription;
    @Nullable
    private String profileAvatarDataUrl;
    private ProfileFieldVisibility profileDescriptionVisibility;
    private ProfileFieldVisibility profileAvatarVisibility;
    private Set<Long> profileDescriptionVisibleCircleIds;
    private Set<Long> profileAvatarVisibleCircleIds;
    private UserLocationSettingsDTO locationSettings;
    private Instant createdAt;
    private long openQuestCount;
    private List<QuestResponseDTO> openQuests;
    private String role;
}
