package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VisionSemanticResponseValidator {

    public void validate(
            VisionPromptUnderstandingResult understanding,
            VisionSemanticOrchestrationRequest request
    ) {
        if (understanding == null || request == null) {
            return;
        }

        VisionSemanticPlan semanticPlan = understanding.semanticPlanOrEmpty();
        VisionIntent candidateIntent = semanticPlan.candidateIntentOrUnsupported();
        if (candidateIntent == VisionIntent.UNSUPPORTED) {
            return;
        }

        Set<String> allowedCandidateIntents = stringSet(request.getResponseContract(), "candidateIntents");
        Set<String> allowedCapabilityIds = stringSet(request.getResponseContract(), "capabilityIds");
        Set<String> allowedFocusSlotIds = stringSet(request.getResponseContract(), "focusSlotIds");

        if (!allowedCandidateIntents.isEmpty() && !allowedCandidateIntents.contains(candidateIntent.name())) {
            throw ServiceErrors.badRequest("Semantic response selected an unsupported candidate intent: " + candidateIntent.name());
        }

        String capabilityId = normalize(semanticPlan.getCapabilityId());
        if (capabilityId == null) {
            throw ServiceErrors.badRequest("Semantic response did not include a capability id");
        }
        if (!allowedCapabilityIds.isEmpty() && !allowedCapabilityIds.contains(capabilityId)) {
            throw ServiceErrors.badRequest("Semantic response selected an unsupported capability id: " + capabilityId);
        }

        String focusSlotId = normalize(understanding.getFocusSlotId());
        if (focusSlotId != null && !allowedFocusSlotIds.isEmpty() && !allowedFocusSlotIds.contains(focusSlotId)) {
            throw ServiceErrors.badRequest("Semantic response selected an unsupported focus slot id: " + focusSlotId);
        }

        VisionSemanticRouteDescriptor selectedRoute = selectRoute(semanticPlan, request.getAllowedRoutes());
        if (selectedRoute == null) {
            return;
        }
        Set<String> allowedSlotIds = allowedSlotIds(selectedRoute);
        validateSemanticPlanFields(semanticPlan, allowedSlotIds);
        validateExtractedSlots(understanding.toExtractedSlotMap(), allowedSlotIds);
    }

    @SuppressWarnings("unchecked")
    private Set<String> stringSet(Map<String, Object> contract, String key) {
        if (contract == null) {
            return Set.of();
        }
        Object value = contract.get(key);
        if (!(value instanceof List<?> list)) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        for (Object item : list) {
            if (item instanceof String string && !string.isBlank()) {
                result.add(string.trim());
            }
        }
        return result;
    }

    private VisionSemanticRouteDescriptor selectRoute(VisionSemanticPlan semanticPlan, List<VisionSemanticRouteDescriptor> allowedRoutes) {
        if (semanticPlan == null || allowedRoutes == null) {
            return null;
        }

        VisionIntent candidateIntent = semanticPlan.candidateIntentOrUnsupported();
        String capabilityId = normalize(semanticPlan.getCapabilityId());
        if (candidateIntent == VisionIntent.UNSUPPORTED || capabilityId == null) {
            return null;
        }

        for (VisionSemanticRouteDescriptor route : allowedRoutes) {
            if (route == null) {
                continue;
            }
            if (candidateIntent.name().equalsIgnoreCase(normalize(route.getIntent()))
                    && capabilityId.equalsIgnoreCase(normalize(route.getCapabilityId()))) {
                return route;
            }
        }
        return null;
    }

    private Set<String> allowedSlotIds(VisionSemanticRouteDescriptor route) {
        if (route == null || route.getSlots() == null) {
            return Set.of();
        }

        Set<String> result = new LinkedHashSet<>();
        for (VisionSemanticSlotDescriptor slot : route.getSlots()) {
            if (slot != null && slot.getSlotId() != null && !slot.getSlotId().isBlank()) {
                result.add(slot.getSlotId().trim());
            }
        }
        if (result.contains("reward_amount")) {
            result.add("free_quest");
        }
        return result;
    }

    private void validateSemanticPlanFields(VisionSemanticPlan semanticPlan, Set<String> allowedSlotIds) {
        if (semanticPlan == null) {
            return;
        }

        if (!semanticPlan.searchQueryOrEmpty().isBlank() && !allowedSlotIds.contains("search_query")) {
            throw ServiceErrors.badRequest("Semantic response selected an unsupported search query for this route");
        }

        String targetUserQuery = normalize(semanticPlan.getTargetUserQuery());
        if (targetUserQuery != null && !allowedSlotIds.contains("target_user")) {
            throw ServiceErrors.badRequest("Semantic response selected an unsupported target user for this route");
        }
    }

    private void validateExtractedSlots(Map<String, String> extractedSlots, Set<String> allowedSlotIds) {
        if (extractedSlots == null || extractedSlots.isEmpty()) {
            return;
        }

        for (String slotId : extractedSlots.keySet()) {
            if (!allowedSlotIds.contains(slotId)) {
                throw ServiceErrors.badRequest("Semantic response selected an unsupported extracted slot id: " + slotId);
            }
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
