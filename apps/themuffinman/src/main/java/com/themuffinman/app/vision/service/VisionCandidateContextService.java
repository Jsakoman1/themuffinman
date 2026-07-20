package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionIntent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisionCandidateContextService {
    private final List<VisionCandidateContextProvider> providers;

    public VisionCandidateContextService(List<VisionCandidateContextProvider> providers) {
        this.providers = providers == null ? List.of() : List.copyOf(providers);
    }

    public VisionCandidateContext build(AppUser currentUser, VisionIntent intent, String requestId, String rawPrompt) {
        if (currentUser == null || intent == null) {
            return null;
        }
        return providers.stream()
                .filter(provider -> provider.supports(intent))
                .findFirst()
                .map(provider -> provider.build(currentUser, intent, requestId, rawPrompt))
                .orElse(null);
    }

    /**
     * Builds candidate context before intent selection. Providers return only their
     * own authorized family; OpenAI still chooses the allowed route and target.
     */
    public List<VisionCandidateContext> buildForSemanticRequest(AppUser currentUser, String requestId, String rawPrompt) {
        if (currentUser == null) {
            return List.of();
        }
        return providers.stream()
                .map(provider -> provider.build(currentUser, null, requestId, rawPrompt))
                .filter(context -> context != null)
                .toList();
    }
}
