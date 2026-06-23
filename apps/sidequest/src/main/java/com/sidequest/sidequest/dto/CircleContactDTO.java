package com.sidequest.sidequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircleContactDTO {
    private Long relationId;
    private Long userId;
    private String username;
    private String profileDescription;
    private String profileAvatarDataUrl;
    private List<Long> circleIds;
    private List<String> circleNames;
}
