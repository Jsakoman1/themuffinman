package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionMemorySummary;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.model.VisionTurnSource;
import com.themuffinman.app.vision.model.VisionUserPreference;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionMemoryFeedbackEventRepository;
import com.themuffinman.app.vision.repository.VisionMemorySummaryRepository;
import com.themuffinman.app.vision.repository.VisionUserPreferenceRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionSemanticOrchestrationContextServiceTest {

    private final VoiceProperties voiceProperties = new VoiceProperties();
    private final VisionSemanticOrchestrationContextService service =
            new VisionSemanticOrchestrationContextService(voiceProperties);

    @Test
    void prefersClientRuntimeHintsOverCountryDefaults() {
        AppUser user = new AppUser();
        user.setId(42L);
        user.setUsername("jsak");
        user.setLocationCountryCode("ch");
        user.setLocationCountry("Switzerland");
        user.setLocationLocality("Zurich");
        user.setLocationLabel("Zurich, Switzerland");

        VisionSemanticUserContext context = service.buildUserContext(user, VisionSemanticRuntimeHints.builder()
                .clientLocale("en-GB")
                .clientTimezone("Europe/London")
                .build());

        assertEquals("en-GB", context.getPreferredLocale());
        assertEquals("en", context.getPreferredLanguage());
        assertEquals("client_runtime_hint", context.getPreferredLocaleSource());
        assertEquals("Europe/London", context.getTimezone());
        assertEquals("client_runtime_hint", context.getTimezoneSource());
        assertEquals("CH", context.getCountryCode());
        assertEquals("Zurich", context.getLocality());
    }

    @Test
    void fallsBackToConfiguredVoiceLocaleBeforeGlobalDefault() {
        voiceProperties.setPreferredLocale("de-CH");

        VisionSemanticUserContext context = service.buildUserContext(null);

        assertEquals("de-CH", context.getPreferredLocale());
        assertEquals("de", context.getPreferredLanguage());
        assertEquals("voice_config_default", context.getPreferredLocaleSource());
        assertEquals("UTC", context.getTimezone());
        assertEquals("default", context.getTimezoneSource());
    }

    @Test
    void usesCountryDefaultsWhenRuntimeHintsAreMissing() {
        AppUser user = new AppUser();
        user.setLocationCountryCode("hr");

        VisionSemanticUserContext context = service.buildUserContext(user, null);

        assertEquals("hr-HR", context.getPreferredLocale());
        assertEquals("country_default", context.getPreferredLocaleSource());
        assertEquals("Europe/Zagreb", context.getTimezone());
        assertEquals("country_default", context.getTimezoneSource());
    }

    @Test
    void buildsMemoryContextFromRecentConversationAndTurns() {
        VisionConversationRepository conversationRepository = mock(VisionConversationRepository.class);
        VisionTurnRepository turnRepository = mock(VisionTurnRepository.class);
        VisionSemanticOrchestrationContextService service = new VisionSemanticOrchestrationContextService(
                voiceProperties,
                conversationRepository,
                turnRepository
        );

        AppUser user = new AppUser();
        user.setId(9L);
        user.setUsername("jsak");
        user.setLocationCountryCode("ch");
        user.setLocationCountry("Switzerland");
        user.setLocationLocality("Zurich");
        user.setLocationLabel("Zurich, Switzerland");

        VisionConversation conversation = new VisionConversation();
        conversation.setId(11L);
        conversation.setOwner(user);
        conversation.setIntent(VisionIntent.CREATE_QUEST);
        conversation.setStatus(VisionConversationStatus.ACTIVE);
        conversation.setRequestedSlot("scheduled_time");
        conversation.setLastUserPrompt("next tuesday");
        conversation.setLastNormalizedPrompt("next tuesday");
        conversation.setLastAssistantMessage("What exact time should I use?");
        conversation.setLastTranslationReliable(true);
        conversation.setSessionMemorySnapshot("{\"sessionSummary\":\"What exact time should I use?\",\"currentIntent\":\"CREATE_QUEST\",\"requestedSlot\":\"scheduled_time\"}");

        VisionConversation recentConversation = new VisionConversation();
        recentConversation.setId(12L);
        recentConversation.setOwner(user);
        recentConversation.setIntent(VisionIntent.VIEW_CIRCLES);
        recentConversation.setStatus(VisionConversationStatus.COMPLETED);
        recentConversation.setRequestedSlot(null);
        recentConversation.setLastAssistantMessage("Here are your circles.");
        recentConversation.setUpdatedAt(Instant.parse("2026-07-03T10:15:30Z"));
        recentConversation.setCreatedAt(Instant.parse("2026-07-03T10:10:30Z"));

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex(1);
        turn.setSource(VisionTurnSource.TEXT);
        turn.setPrompt("create quest");
        turn.setNormalizedPrompt("create quest");
        turn.setDetectedIntent(VisionIntent.CREATE_QUEST);
        turn.setRequestedSlot("scheduled_time");
        turn.setAssistantMessage("What exact time should I use?");

        when(conversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user)).thenReturn(List.of(recentConversation, conversation));
        when(turnRepository.findTop10ByConversationOrderByTurnIndexDesc(conversation)).thenReturn(List.of(turn));

        var memoryContext = service.buildMemoryContext(user, conversation);

        assertEquals("jsak", memoryContext.getUserMemory().getUsername());
        assertEquals("CREATE_QUEST", memoryContext.getSessionMemory().getCurrentIntent());
        assertEquals("quests", memoryContext.getSessionMemory().getCurrentEntityFamily());
        assertEquals("scheduled_time", memoryContext.getSessionMemory().getRequestedSlot());
        assertEquals("What exact time should I use?", memoryContext.getSessionMemory().getSessionSummary());
        assertEquals(List.of("What exact time should I use?"), memoryContext.getSessionMemory().getOpenQuestions());
        assertTrue(memoryContext.getSessionMemory().getRecentActions().get(0).contains("create quest"));
        assertEquals("{\"sessionSummary\":\"What exact time should I use?\",\"currentIntent\":\"CREATE_QUEST\",\"requestedSlot\":\"scheduled_time\"}", memoryContext.getSessionMemory().getSessionMemorySnapshot());
        assertEquals(1, memoryContext.getSessionMemory().getRecentTurns().size());
        assertEquals("VIEW_CIRCLES", memoryContext.getRecentConversations().get(0).getIntent());
        assertTrue(memoryContext.getUserMemory().getRecentIntentTypes().contains("CREATE_QUEST"));
        assertTrue(memoryContext.getUserMemory().getRecentEntityFamilies().contains("quests"));
    }

    @Test
    void includesLearnedPreferencesAndSummaryWhenAvailable() {
        VisionConversationRepository conversationRepository = mock(VisionConversationRepository.class);
        VisionTurnRepository turnRepository = mock(VisionTurnRepository.class);
        VisionUserPreferenceRepository preferenceRepository = mock(VisionUserPreferenceRepository.class);
        VisionMemoryFeedbackEventRepository feedbackRepository = mock(VisionMemoryFeedbackEventRepository.class);
        VisionMemorySummaryRepository summaryRepository = mock(VisionMemorySummaryRepository.class);

        VisionSemanticOrchestrationContextService service = new VisionSemanticOrchestrationContextService(
                voiceProperties,
                conversationRepository,
                turnRepository,
                preferenceRepository,
                feedbackRepository,
                summaryRepository
        );

        AppUser user = new AppUser();
        user.setId(12L);
        user.setUsername("jsak");

        VisionUserPreference preferredInput = new VisionUserPreference();
        preferredInput.setUser(user);
        preferredInput.setPreferenceKey("preferred_input_type");
        preferredInput.setPreferenceValue("voice");

        VisionUserPreference preferredFamily = new VisionUserPreference();
        preferredFamily.setUser(user);
        preferredFamily.setPreferenceKey("last_entity_family");
        preferredFamily.setPreferenceValue("quests");

        VisionMemorySummary summary = new VisionMemorySummary();
        summary.setUser(user);
        summary.setSummaryText("Top preferences: [preferred_input_type=voice]");

        when(preferenceRepository.findByUserAndPreferenceKey(user, "preferred_input_type")).thenReturn(java.util.Optional.of(preferredInput));
        when(preferenceRepository.findByUserAndPreferenceKey(user, "last_entity_family")).thenReturn(java.util.Optional.of(preferredFamily));
        when(summaryRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(java.util.Optional.of(summary));
        when(feedbackRepository.findTop20ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(conversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user)).thenReturn(List.of());

        var memoryContext = service.buildMemoryContext(user, null);

        assertEquals("voice", memoryContext.getUserMemory().getPreferredInputType());
        assertEquals("quests", memoryContext.getUserMemory().getPreferredEntityFamily());
        assertEquals("Top preferences: [preferred_input_type=voice]", memoryContext.getUserMemory().getLearningSummary());
        assertTrue(memoryContext.getUserMemory().getRecentFeedbackTypes().isEmpty());
    }
}
