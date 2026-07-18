package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import org.springframework.lang.Nullable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionSearchComparisonDTO {
    private String capabilityId;
    private String query;
    private int selectionLimit;
    private int omittedSelectionCount;
    @Nullable
    private String fallbackMessage;
    private List<String> comparableFields;
    private List<VisionSearchComparisonItemDTO> items;
}
