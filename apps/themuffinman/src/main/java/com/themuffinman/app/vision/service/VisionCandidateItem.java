package com.themuffinman.app.vision.service;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/** Compact, privacy-safe candidate representation for one semantic request. */
@Getter
@Builder
public class VisionCandidateItem {
    private String stableCandidateId;
    private String family;
    private String titleOrLabel;
    private String compactSummary;
    private Map<String, String> searchableFields;
    private Map<String, String> viewerSafeMetadata;
    private List<String> allowedActionHints;
}
