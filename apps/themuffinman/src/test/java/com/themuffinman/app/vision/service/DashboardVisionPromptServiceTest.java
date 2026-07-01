package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.service.AdminAgentPlaygroundService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.DashboardVisionPromptRequestDTO;
import com.themuffinman.app.vision.dto.DashboardVisionPromptResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardVisionPromptServiceTest {

    @Mock
    private AdminAgentPlaygroundService adminAgentPlaygroundService;

    @Test
    void promptServiceNormalizesAndClassifiesPrompt() {
        when(adminAgentPlaygroundService.analyzePrompt("Pokaži mi nešto blizu danas"))
                .thenReturn(AdminAgentPlaygroundResponseDTO.builder()
                        .promptSourceLanguage("hr")
                        .promptTranslationProvider("openai")
                        .promptTranslationApplied(true)
                        .promptTranslationReliable(true)
                        .translatedPrompt("Show me something nearby today")
                        .summary("Live agent summary")
                        .matchedSignals(java.util.List.of("filter_nearby", "mode_browse"))
                        .build());

        DashboardVisionPromptService service = new DashboardVisionPromptService(adminAgentPlaygroundService);
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
        assertEquals("Live agent summary", response.getAssistantNote());
        assertFalse(response.getMatchedSignals().isEmpty());
    }

    @Test
    void blankPromptIsRejected() {
        DashboardVisionPromptService service = new DashboardVisionPromptService(adminAgentPlaygroundService);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.process(DashboardVisionPromptRequestDTO.builder().prompt(" ").build(), user()));

        assertEquals(400, exception.getStatusCode().value());
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(21L);
        return user;
    }
}
