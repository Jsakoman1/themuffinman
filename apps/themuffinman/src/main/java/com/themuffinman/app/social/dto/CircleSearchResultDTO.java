package com.themuffinman.app.social.dto;

import com.themuffinman.app.identity.dto.ProfilePrimaryActionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircleSearchResultDTO {
    private Long id;
    private String username;
    private String profileDescription;
    private String profileAvatarDataUrl;
    private String email;
    private String locationLabel;
    private Double distanceKm;
    private String distanceLabel;
    private String resolutionKey;
    private String resolutionLabel;
    private boolean exactResolutionEligible;
    private CircleRelationStatusDTO relationStatus;
    private String relationLabel;
    private String relationBadgeClass;
    private ProfilePrimaryActionDTO primaryAction;
    private ProfilePrimaryActionDTO secondaryAction;
    private boolean blockedByCurrentUser;
}
