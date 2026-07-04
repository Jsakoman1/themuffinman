package com.themuffinman.app.vision.dto;

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
public class VisionSearchDiscoveryItemDTO {
    private String entityFamily;
    private String capabilityId;
    private Long targetId;
    private String title;
    private String summary;
    private String matchSummary;
    private String resolutionLabel;
    private boolean exactResolutionEligible;
}
