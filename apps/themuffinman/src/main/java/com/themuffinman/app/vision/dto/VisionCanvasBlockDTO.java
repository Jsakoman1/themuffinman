package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionCanvasBlockDTO {
    private String type;
    private String title;
    private String body;
    private String fieldId;
    private String fieldKind;
    private boolean required;
    private String placeholder;
    private List<VisionOptionDTO> options;
    private List<VisionSlotSummaryDTO> items;
    private VisionQuestDiscoveryDTO questDiscovery;
    private VisionSearchDiscoveryDTO searchDiscovery;
    private VisionQuestReviewDTO review;
    private String tone;
}
