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
public class VisionLearningMemoryDTO {
    private String summaryText;
    private List<String> recentFeedbackTypes;
    private List<VisionLearningPreferenceDTO> preferenceSignals;
    private List<VisionLearningExplainabilityDTO> explainabilityRecords;
}
