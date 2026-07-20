package com.themuffinman.app.vision.service;

import java.util.List;

final class VisionCandidateContextBuilder {
    static final int COMPLETE_CANDIDATE_LIMIT = 50;
    private VisionCandidateContextBuilder() {
    }

    static VisionCandidateContext complete(
            String family,
            String scope,
            String requestId,
            List<VisionCandidateItem> items,
            String retrievalStrategy
    ) {
        List<VisionCandidateItem> safeItems = items == null ? List.of() : List.copyOf(items);
        return VisionCandidateContext.builder()
                .contractVersion(VisionCandidateContext.CONTRACT_VERSION)
                .family(family)
                .scope(scope)
                .requestId(requestId)
                .complete(true)
                .totalCandidates(safeItems.size())
                .returnedCandidates(safeItems.size())
                .retrievalStrategy(retrievalStrategy == null ? "authorized_read_model_complete" : retrievalStrategy)
                .items(safeItems)
                .build();
    }

    static VisionCandidateContext partial(
            String family,
            String scope,
            String requestId,
            int totalCandidates,
            List<VisionCandidateItem> items,
            String retrievalStrategy
    ) {
        List<VisionCandidateItem> safeItems = items == null ? List.of() : List.copyOf(items);
        int safeTotal = Math.max(totalCandidates, safeItems.size());
        return VisionCandidateContext.builder()
                .contractVersion(VisionCandidateContext.CONTRACT_VERSION)
                .family(family)
                .scope(scope)
                .requestId(requestId)
                .complete(false)
                .totalCandidates(safeTotal)
                .returnedCandidates(safeItems.size())
                .retrievalStrategy(retrievalStrategy == null ? "authorized_retrieval_partial" : retrievalStrategy)
                .items(safeItems)
                .build();
    }

    static VisionCandidateContext completeOrPartial(
            String family,
            String scope,
            String requestId,
            List<VisionCandidateItem> items,
            String retrievalStrategy
    ) {
        List<VisionCandidateItem> safeItems = items == null ? List.of() : List.copyOf(items);
        if (safeItems.size() <= COMPLETE_CANDIDATE_LIMIT) {
            return complete(family, scope, requestId, safeItems, retrievalStrategy);
        }
        return partial(family, scope, requestId, safeItems.size(),
                safeItems.subList(0, COMPLETE_CANDIDATE_LIMIT),
                retrievalStrategy == null ? "authorized_retrieval_partial" : retrievalStrategy + "_partial");
    }
}
