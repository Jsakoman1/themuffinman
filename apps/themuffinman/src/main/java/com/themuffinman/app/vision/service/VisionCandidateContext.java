package com.themuffinman.app.vision.service;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Request-scoped, viewer-authorized context offered to semantic understanding.
 * This is deliberately separate from durable Vision memory and is never an
 * authorization source by itself.
 */
@Getter
@Builder
public class VisionCandidateContext {
    public static final String CONTRACT_VERSION = "vision-candidate-context-v1";

    private String contractVersion;
    private String family;
    private String scope;
    private String requestId;
    private boolean complete;
    private int totalCandidates;
    private int returnedCandidates;
    private String retrievalStrategy;
    private List<VisionCandidateItem> items;

    public static VisionCandidateContext empty(String family, String scope, String requestId) {
        return VisionCandidateContext.builder()
                .contractVersion(CONTRACT_VERSION)
                .family(family)
                .scope(scope)
                .requestId(requestId)
                .complete(true)
                .totalCandidates(0)
                .returnedCandidates(0)
                .retrievalStrategy("none")
                .items(List.of())
                .build();
    }
}
