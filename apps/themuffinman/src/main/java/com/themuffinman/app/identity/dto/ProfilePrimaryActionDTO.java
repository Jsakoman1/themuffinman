package com.themuffinman.app.identity.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePrimaryActionDTO {
    private String type;
    private String label;
    private boolean enabled;
    private NavigationTargetDTO navigation;
}
