package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
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
import java.time.Clock;
import java.time.ZoneOffset;
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
                new VisionProperties(),
                conversationRepository,
                turnRepository,
                preferenceRepository,
                feedbackRepository,
                summaryRepository,
                Clock.fixed(Instant.parse("2026-07-03T10:10:00Z"), ZoneOffset.UTC)
        );

        AppUser user = new AppUser();
        user.setId(12L);
        user.setUsername("jsak");

        VisionUserPreference preferredInput = new VisionUserPreference();
        preferredInput.setUser(user);
        preferredInput.setPreferenceKey("preferred_input_type");
        preferredInput.setPreferenceValue("voice");
        preferredInput.setConfidenceScore(0.88d);
        preferredInput.setConfidenceUpdatedAt(Instant.parse("2026-07-03T10:00:00Z"));

        VisionUserPreference preferredFamily = new VisionUserPreference();
        preferredFamily.setUser(user);
        preferredFamily.setPreferenceKey("last_entity_family");
        preferredFamily.setPreferenceValue("quests");
        preferredFamily.setConfidenceScore(0.79d);
        preferredFamily.setConfidenceUpdatedAt(Instant.parse("2026-07-03T10:00:00Z"));

        VisionUserPreference recentFeedback = new VisionUserPreference();
        recentFeedback.setUser(user);
        recentFeedback.setPreferenceKey("last_feedback_type");
        recentFeedback.setPreferenceValue("correction");
        recentFeedback.setConfidenceScore(0.98d);
        recentFeedback.setConfidenceUpdatedAt(Instant.parse("2026-07-03T10:05:00Z"));

        VisionUserPreference lastIntent = new VisionUserPreference();
        lastIntent.setUser(user);
        lastIntent.setPreferenceKey("last_intent");
        lastIntent.setPreferenceValue("create_quest");
        lastIntent.setConfidenceScore(0.83d);
        lastIntent.setConfidenceUpdatedAt(Instant.parse("2026-07-03T10:02:00Z"));

        VisionUserPreference lastRequestedSlot = new VisionUserPreference();
        lastRequestedSlot.setUser(user);
        lastRequestedSlot.setPreferenceKey("last_requested_slot");
        lastRequestedSlot.setPreferenceValue("scheduled_time");
        lastRequestedSlot.setConfidenceScore(0.81d);
        lastRequestedSlot.setConfidenceUpdatedAt(Instant.parse("2026-07-03T10:01:00Z"));

        VisionMemorySummary summary = new VisionMemorySummary();
        summary.setUser(user);
        summary.setSummaryText("Top preferences: [preferred_input_type=voice]");

        when(preferenceRepository.findByUserAndPreferenceKey(user, "preferred_input_type")).thenReturn(java.util.Optional.of(preferredInput));
        when(preferenceRepository.findByUserAndPreferenceKey(user, "last_entity_family")).thenReturn(java.util.Optional.of(preferredFamily));
        when(preferenceRepository.findByUser(user)).thenReturn(List.of(recentFeedback, lastIntent, lastRequestedSlot, preferredFamily, preferredInput));
        when(summaryRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(java.util.Optional.of(summary));
        when(feedbackRepository.findTop20ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(conversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user)).thenReturn(List.of());

        var memoryContext = service.buildMemoryContext(user, null);

        assertEquals("voice", memoryContext.getUserMemory().getPreferredInputType());
        assertTrue(memoryContext.getUserMemory().getPreferredInputTypeConfidence() < 0.88d);
        assertTrue(memoryContext.getUserMemory().getPreferredInputTypeConfidence() > 0.30d);
        assertEquals("quests", memoryContext.getUserMemory().getPreferredEntityFamily());
        assertTrue(memoryContext.getUserMemory().getPreferredEntityFamilyConfidence() < 0.79d);
        assertTrue(memoryContext.getUserMemory().getPreferredEntityFamilyConfidence() > 0.30d);
        assertTrue(memoryContext.getUserMemory().getLearningSummary().contains("preferred_input_type=voice"));
        assertTrue(memoryContext.getUserMemory().getRetrievalSummary().contains("retrieval_focus=quests"));
        assertTrue(memoryContext.getUserMemory().getRecentFeedbackTypes().isEmpty());
        assertEquals(5, memoryContext.getUserMemory().getLearnedPreferences().size());
        assertEquals("preferred_input_type", memoryContext.getUserMemory().getLearnedPreferences().get(0).getPreferenceKey());
        assertEquals(5, memoryContext.getUserMemory().getExplainabilityRecords().size());
        assertEquals("habit_selection", memoryContext.getUserMemory().getExplainabilityRecords().get(0).getDecisionType());
        assertEquals("intent_selection", memoryContext.getUserMemory().getExplainabilityRecords().get(2).getDecisionType());
        assertEquals("slot_focus", memoryContext.getUserMemory().getExplainabilityRecords().get(3).getDecisionType());
    }

    @Test
    void fallsBackToRecentTopicFamilyWhenPreferredFamilyConfidenceIsWeak() {
        VisionConversationRepository conversationRepository = mock(VisionConversationRepository.class);
        VisionTurnRepository turnRepository = mock(VisionTurnRepository.class);
        VisionUserPreferenceRepository preferenceRepository = mock(VisionUserPreferenceRepository.class);
        VisionMemoryFeedbackEventRepository feedbackRepository = mock(VisionMemoryFeedbackEventRepository.class);
        VisionMemorySummaryRepository summaryRepository = mock(VisionMemorySummaryRepository.class);

        VisionSemanticOrchestrationContextService service = new VisionSemanticOrchestrationContextService(
                voiceProperties,
                new VisionProperties(),
                conversationRepository,
                turnRepository,
                preferenceRepository,
                feedbackRepository,
                summaryRepository
        );

        AppUser user = new AppUser();
        user.setId(13L);
        user.setUsername("jsak");

        VisionUserPreference weakFamily = new VisionUserPreference();
        weakFamily.setUser(user);
        weakFamily.setPreferenceKey("last_entity_family");
        weakFamily.setPreferenceValue("circles");
        weakFamily.setConfidenceScore(0.18d);
        weakFamily.setConfidenceUpdatedAt(Instant.parse("2026-07-03T10:00:00Z"));

        VisionMemorySummary summary = new VisionMemorySummary();
        summary.setUser(user);
        summary.setSummaryText("Memory summary stays visible even when family confidence is weak.");

        VisionConversation circlesConversation = new VisionConversation();
        circlesConversation.setId(20L);
        circlesConversation.setOwner(user);
        circlesConversation.setIntent(VisionIntent.VIEW_CIRCLES);
        circlesConversation.setStatus(VisionConversationStatus.COMPLETED);
        circlesConversation.setCreatedAt(Instant.parse("2026-07-03T10:10:30Z"));
        circlesConversation.setUpdatedAt(Instant.parse("2026-07-03T10:15:30Z"));

        VisionConversation questConversation = new VisionConversation();
        questConversation.setId(21L);
        questConversation.setOwner(user);
        questConversation.setIntent(VisionIntent.CREATE_QUEST);
        questConversation.setStatus(VisionConversationStatus.ACTIVE);
        questConversation.setCreatedAt(Instant.parse("2026-07-03T09:10:30Z"));
        questConversation.setUpdatedAt(Instant.parse("2026-07-03T09:15:30Z"));

        when(preferenceRepository.findByUserAndPreferenceKey(user, "last_entity_family")).thenReturn(java.util.Optional.of(weakFamily));
        when(preferenceRepository.findByUser(user)).thenReturn(List.of(weakFamily));
        when(summaryRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(java.util.Optional.of(summary));
        when(feedbackRepository.findTop20ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(conversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user)).thenReturn(List.of(circlesConversation, questConversation));

        var memoryContext = service.buildMemoryContext(user, null);

        assertEquals("circles", memoryContext.getUserMemory().getRecentEntityFamilies().get(0));
        assertEquals("circles", memoryContext.getUserMemory().getRetrievedEntityFamily());
        assertTrue(memoryContext.getUserMemory().getRetrievedEntityFamilyConfidence() >= 0.30d);
        assertTrue(memoryContext.getUserMemory().getRetrievalSummary().contains("retrieval_focus=circles"));
        assertTrue(memoryContext.getUserMemory().getLearningSummary().contains("Memory summary"));
        assertTrue(memoryContext.getUserMemory().getPreferredEntityFamily() == null);
    }
}
