package com.themuffinman.app.vision.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionCandidateContextContractTest {

    @Test
    void emptyContextIsCompleteAndExplicitlyNotAnAuthorizationSource() {
        VisionCandidateContext context = VisionCandidateContext.empty("quest", "current_user_owned_quests", "req-1");

        assertEquals("vision-candidate-context-v1", context.getContractVersion());
        assertEquals("current_user_owned_quests", context.getScope());
        assertTrue(context.isComplete());
        assertEquals(0, context.getTotalCandidates());
        assertEquals(List.of(), context.getItems());
    }

    @Test
    void candidateCarriesOnlyCompactSemanticFields() {
        VisionCandidateItem item = VisionCandidateItem.builder()
                .stableCandidateId("quest:42")
                .family("quest")
                .titleOrLabel("Move sofa")
                .compactSummary("Open quest")
                .searchableFields(Map.of("title", "Move sofa"))
                .viewerSafeMetadata(Map.of("status", "OPEN"))
                .allowedActionHints(List.of("OPEN"))
                .build();

        assertEquals("quest:42", item.getStableCandidateId());
        assertEquals("Move sofa", item.getSearchableFields().get("title"));
        assertEquals("OPEN", item.getViewerSafeMetadata().get("status"));
    }
}
