package com.themuffinman.app.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircleGroupResponseDTO {
    private Long id;
    private String name;
    private String resolutionKey;
    private String resolutionLabel;
    private boolean exactResolutionEligible;
    private int memberCount;
    private String memberPreviewLabel;
    private List<CircleMemberDTO> members;
}
