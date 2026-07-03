package com.themuffinman.app.semantic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SemanticEnvelope {

    private String rawUserText;
    private String normalizedEnglishText;
    private String localizedDisplayText;
    private String sourceLanguage;
    private String contractVersion;
    private String intent;
    private SemanticEntityFamily entityFamily;
    private String targetEntityQuery;
    private SemanticEntityResolutionStatus entityResolutionStatus;
    private String entityResolutionLabel;
    private Double entityResolutionConfidence;
    private String entityResolutionReason;
    @Builder.Default
    private Map<String, String> slotCandidates = new LinkedHashMap<>();
    private Double confidence;
    private String ambiguityReason;
    private boolean clarificationRequired;
    @Builder.Default
    private List<String> requiredSlotIds = List.of();
    @Builder.Default
    private List<String> missingRequiredSlotIds = List.of();
    private SemanticReplayRecord replayRecord;
    private String translationProvider;
    private boolean translationApplied;
    private boolean translationReliable;

    public String normalizedEnglishTextOrRaw() {
        if (normalizedEnglishText != null && !normalizedEnglishText.isBlank()) {
            return normalizedEnglishText.trim();
        }
        return rawUserText == null ? "" : rawUserText.trim();
    }

    public Map<String, String> slotCandidatesOrEmpty() {
        return slotCandidates == null ? Map.of() : Map.copyOf(slotCandidates);
    }

    public List<String> requiredSlotIdsOrEmpty() {
        return requiredSlotIds == null ? List.of() : List.copyOf(requiredSlotIds);
    }

    public List<String> missingRequiredSlotIdsOrEmpty() {
        return missingRequiredSlotIds == null ? List.of() : List.copyOf(missingRequiredSlotIds);
    }
}
