package com.themuffinman.app.social.dto;

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
public class CircleRelationDTO {
    private CircleRelationStatusDTO relationStatus;
    private String relationLabel;
    private String relationBadgeClass;
    private boolean blockedByCurrentUser;
    private boolean exactResolutionEligible;
}
