package com.sidequest.sidequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircleMemberDTO {
    private Long userId;
    private String username;
    private String profileDescription;
    private String profileAvatarDataUrl;
}
