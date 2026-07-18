package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionSearchComparisonItemDTO {
    private String entityFamily;
    private Long targetId;
    private String title;
    private String sourceRoute;
    private Map<String, String> fields;
}
