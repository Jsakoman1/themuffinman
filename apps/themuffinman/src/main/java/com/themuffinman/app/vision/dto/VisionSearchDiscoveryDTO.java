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
public class VisionSearchDiscoveryDTO {
    private String contractVersion;
    private String capabilityId;
    private String query;
    private String filterFamily;
    private String filterSchemaVersion;
    private List<String> availableEntityFamilies;
    private String sort;
    private int page;
    private int pageSize;
    private String summary;
    private String resultState;
    private String recoveryAction;
    private VisionSearchRecoveryCode recoveryCode;
    private long totalItems;
    private boolean hasMore;
    private List<VisionSearchDiscoveryItemDTO> items;
}
