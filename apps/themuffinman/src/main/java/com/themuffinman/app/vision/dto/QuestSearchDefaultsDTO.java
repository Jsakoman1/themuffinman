package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestSearchDefaultsDTO {
    private String defaultSort;
    private Integer defaultRadiusKm;
    private List<Integer> radiusOptionsKm;
    private boolean hasViewerLocation;
    private boolean nearbyDefaultEnabled;
}
