package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.service.AdminAgentPlaygroundService;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.DashboardVisionPromptRequestDTO;
import com.themuffinman.app.workmarket.dto.DashboardVisionPromptResponseDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DashboardVisionPromptService {

    private final AdminAgentPlaygroundService adminAgentPlaygroundService;

    public DashboardVisionPromptService(AdminAgentPlaygroundService adminAgentPlaygroundService) {
        this.adminAgentPlaygroundService = adminAgentPlaygroundService;
    }

    public DashboardVisionPromptResponseDTO process(DashboardVisionPromptRequestDTO dto, AppUser currentUser) {
        validateAccess(currentUser);

        if (dto == null || dto.getPrompt() == null || dto.getPrompt().isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }

        String prompt = dto.getPrompt().trim();
        AdminAgentPlaygroundResponseDTO agentResponse = adminAgentPlaygroundService.analyzePrompt(prompt);
        String normalizedPrompt = agentResponse.getTranslatedPrompt() != null && !agentResponse.getTranslatedPrompt().isBlank()
                ? agentResponse.getTranslatedPrompt().trim()
                : prompt;

        Set<String> matchedSignals = new LinkedHashSet<>();
        String activeFilter = deriveActiveFilter(normalizedPrompt, matchedSignals);
        String surfaceMode = deriveSurfaceMode(normalizedPrompt, matchedSignals);
        matchedSignals.addAll(agentResponse.getMatchedSignals());

        String assistantNote = agentResponse.getSummary() != null && !agentResponse.getSummary().isBlank()
                ? agentResponse.getSummary()
                : buildAssistantNote(normalizedPrompt, activeFilter, surfaceMode);

        return DashboardVisionPromptResponseDTO.builder()
                .prompt(prompt)
                .normalizedPrompt(normalizedPrompt)
                .source(dto.getSource() != null && !dto.getSource().isBlank() ? dto.getSource().trim() : "text")
                .translationProvider(agentResponse.getPromptTranslationProvider())
                .translationApplied(agentResponse.isPromptTranslationApplied())
                .translationReliable(agentResponse.isPromptTranslationReliable())
                .activeFilter(activeFilter)
                .surfaceMode(surfaceMode)
                .assistantNote(assistantNote)
                .matchedSignals(List.copyOf(matchedSignals))
                .build();
    }

    private String deriveActiveFilter(String prompt, Set<String> matchedSignals) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "nearby", "near me", "close to home", "close by", "local", "around me")) {
            matchedSignals.add("filter_nearby");
            return "nearby";
        }

        if (containsAny(lower, "today", "right now", "now", "tonight")) {
            matchedSignals.add("filter_today");
            return "today";
        }

        matchedSignals.add("filter_best_match");
        return "best-match";
    }

    private String deriveSurfaceMode(String prompt, Set<String> matchedSignals) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "compare", "versus", "vs", "different options", "side by side")) {
            matchedSignals.add("mode_compare");
            return "compare";
        }

        if (containsAny(lower, "focus", "only", "single", "just this", "one option")) {
            matchedSignals.add("mode_focus");
            return "focus";
        }

        matchedSignals.add("mode_browse");
        return "browse";
    }

    private String buildAssistantNote(String prompt, String activeFilter, String surfaceMode) {
        return "Decoded prompt and shifted the surface to %s mode with %s matching.".formatted(surfaceMode, activeFilter)
                + " Prompt: " + prompt;
    }

    private boolean containsAny(String value, String... patterns) {
        for (String pattern : patterns) {
            if (value.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private void validateAccess(AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
    }
}
