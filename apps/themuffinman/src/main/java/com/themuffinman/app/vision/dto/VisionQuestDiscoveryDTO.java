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
public class VisionQuestDiscoveryDTO {
    private String capabilityId;
    private String query;
    private String sort;
    private String summary;
    private long totalItems;
    private boolean hasMore;
    private List<VisionQuestDiscoveryItemDTO> items;
}
