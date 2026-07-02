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
public class VisionCapabilityPreviewDTO {
    private String capabilityId;
    private String title;
    private String summary;
    private List<VisionSlotSummaryDTO> items;
    private String tone;
}
