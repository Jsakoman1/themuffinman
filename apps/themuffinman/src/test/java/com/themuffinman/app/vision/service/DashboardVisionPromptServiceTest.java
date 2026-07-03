package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.DashboardVisionPromptRequestDTO;
import com.themuffinman.app.vision.dto.DashboardVisionPromptResponseDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DashboardVisionPromptServiceTest {

    @Test
    void promptServiceUsesVisionSemanticUnderstandingAndNormalizesPrompt() {
        VisionPromptUnderstandingService understandingService = new StubVisionPromptUnderstandingService(
                VisionPromptUnderstandingResult.builder()
                        .normalizedPrompt("Show me something nearby today")
                        .translationProvider("openai")
                        .translationApplied(true)
                        .translationReliable(true)
                        .semanticPlan(VisionSemanticPlan.discoverQuests(0.94d, "Browse matching results", "moving help"))
                        .build()
        );

        DashboardVisionPromptService service = new DashboardVisionPromptService(understandingService);
        DashboardVisionPromptResponseDTO response = service.process(
                DashboardVisionPromptRequestDTO.builder()
                        .prompt("Pokaži mi nešto blizu danas")
                        .source("text")
                        .build(),
                user()
        );

        assertEquals("Pokaži mi nešto blizu danas", response.getPrompt());
        assertEquals("Show me something nearby today", response.getNormalizedPrompt());
        assertEquals("openai", response.getTranslationProvider());
        assertEquals("nearby", response.getActiveFilter());
        assertEquals("browse", response.getSurfaceMode());
        assertEquals("Browse matching results", response.getAssistantNote());
        assertFalse(response.getMatchedSignals().isEmpty());
    }

    @Test
    void blankPromptIsRejected() {
        DashboardVisionPromptService service = new DashboardVisionPromptService(new StubVisionPromptUnderstandingService(VisionPromptUnderstandingResult.empty("")));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.process(DashboardVisionPromptRequestDTO.builder().prompt(" ").build(), user()));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rejectsUnauthenticatedAccess() {
        DashboardVisionPromptService service = new DashboardVisionPromptService(new StubVisionPromptUnderstandingService(VisionPromptUnderstandingResult.empty("")));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.process(DashboardVisionPromptRequestDTO.builder().prompt("hello").build(), null));

        assertEquals(403, exception.getStatusCode().value());
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(21L);
        return user;
    }

    private static final class StubVisionPromptUnderstandingService extends VisionPromptUnderstandingService {
        private final VisionPromptUnderstandingResult result;

        private StubVisionPromptUnderstandingService(VisionPromptUnderstandingResult result) {
            super(
                    new com.themuffinman.app.config.AgentProperties(),
                    new com.themuffinman.app.agent.service.LocalAdminAgentPromptTranslator(),
                    new VisionSemanticMapper(),
                    new com.themuffinman.app.prompt.PromptSemanticsSupport(),
                    new VisionSemanticOrchestrationContextService(new com.themuffinman.app.config.VoiceProperties()),
                    new VisionSemanticRouteCatalogService(),
                    new VisionSemanticContractSanitizer(),
                    new VisionSemanticResponseValidator()
            );
            this.result = result;
        }

        @Override
        public VisionPromptUnderstandingResult understandPrompt(String prompt, VisionConversation conversation, AppUser currentUser, VisionSemanticRuntimeHints runtimeHints) {
            return result;
        }
    }
}
