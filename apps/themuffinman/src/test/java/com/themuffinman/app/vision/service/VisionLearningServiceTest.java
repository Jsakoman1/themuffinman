package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationAction;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionMemoryFeedbackEvent;
import com.themuffinman.app.vision.model.VisionMemoryFeedbackType;
import com.themuffinman.app.vision.model.VisionMemorySummary;
import com.themuffinman.app.vision.model.VisionMemorySummaryKind;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.model.VisionTurnSource;
import com.themuffinman.app.vision.model.VisionUserPreference;
import com.themuffinman.app.vision.repository.VisionMemoryFeedbackEventRepository;
import com.themuffinman.app.vision.repository.VisionMemorySummaryRepository;
import com.themuffinman.app.vision.repository.VisionUserPreferenceRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisionLearningServiceTest {

    @Test
    void recordsFeedbackAndPreferenceSignalsFromTurnOutcome() {
        VisionUserPreferenceRepository preferenceRepository = mock(VisionUserPreferenceRepository.class);
        VisionMemoryFeedbackEventRepository feedbackRepository = mock(VisionMemoryFeedbackEventRepository.class);
        VisionMemorySummaryRepository summaryRepository = mock(VisionMemorySummaryRepository.class);
        VisionProperties visionProperties = new VisionProperties();

        when(preferenceRepository.findByUserAndPreferenceKey(any(), any())).thenReturn(Optional.empty());

        VisionLearningService service = new VisionLearningService(
                preferenceRepository,
                feedbackRepository,
                summaryRepository,
                visionProperties
        );

        AppUser user = new AppUser();
        user.setId(5L);
        user.setUsername("assistant-user");

        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(user);
        conversation.setIntent(VisionIntent.CREATE_QUEST);
        conversation.setStatus(VisionConversationStatus.REVIEW_READY);
        conversation.setRequestedSlot("scheduled_time");

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setSource(VisionTurnSource.VOICE);
        turn.setDetectedIntent(VisionIntent.CREATE_QUEST);
        turn.setRequestedSlot("scheduled_time");
        turn.setPrompt("what time");
        turn.setNormalizedPrompt("what time");
        turn.setAssistantMessage("What exact time should I use?");

        service.recordTurnOutcome(
                user,
                conversation,
                turn,
                VisionPromptUnderstandingResult.builder().sourceLanguage("hr").build(),
                VisionSemanticRuntimeHints.builder()
                        .inputType("voice")
                        .build(),
                VisionConversationAction.REQUEST_REVIEW_EDIT
        );

        verify(feedbackRepository).save(any(VisionMemoryFeedbackEvent.class));
        verify(preferenceRepository, atLeastOnce()).save(any(VisionUserPreference.class));
    }

    @Test
    void compactsRecentLearningIntoSummary() {
        VisionUserPreferenceRepository preferenceRepository = mock(VisionUserPreferenceRepository.class);
        VisionMemoryFeedbackEventRepository feedbackRepository = mock(VisionMemoryFeedbackEventRepository.class);
        VisionMemorySummaryRepository summaryRepository = mock(VisionMemorySummaryRepository.class);
        VisionProperties visionProperties = new VisionProperties();
        visionProperties.getMemory().setSummaryWindow(2);
        visionProperties.getMemory().setRecentFeedbackWindow(2);

        VisionLearningService service = new VisionLearningService(
                preferenceRepository,
                feedbackRepository,
                summaryRepository,
                visionProperties
        );

        AppUser user = new AppUser();
        user.setId(6L);
        user.setUsername("assistant-user");

        VisionUserPreference preferredInput = new VisionUserPreference();
        preferredInput.setUser(user);
        preferredInput.setPreferenceKey("preferred_input_type");
        preferredInput.setPreferenceValue("voice");
        preferredInput.setObservationCount(3);
        preferredInput.setLastObservedAt(Instant.parse("2026-07-01T10:00:00Z"));

        VisionUserPreference preferredEntity = new VisionUserPreference();
        preferredEntity.setUser(user);
        preferredEntity.setPreferenceKey("last_entity_family");
        preferredEntity.setPreferenceValue("quests");
        preferredEntity.setObservationCount(2);
        preferredEntity.setLastObservedAt(Instant.parse("2026-07-02T10:00:00Z"));

        VisionUserPreference recentFeedback = new VisionUserPreference();
        recentFeedback.setUser(user);
        recentFeedback.setPreferenceKey("last_feedback_type");
        recentFeedback.setPreferenceValue("correction");
        recentFeedback.setObservationCount(5);
        recentFeedback.setConfidenceScore(0.99d);
        recentFeedback.setLastObservedAt(Instant.parse("2026-07-03T10:00:00Z"));

        VisionUserPreference lastIntent = new VisionUserPreference();
        lastIntent.setUser(user);
        lastIntent.setPreferenceKey("last_intent");
        lastIntent.setPreferenceValue("create_quest");
        lastIntent.setObservationCount(4);
        lastIntent.setConfidenceScore(0.91d);
        lastIntent.setLastObservedAt(Instant.parse("2026-07-03T09:30:00Z"));

        VisionUserPreference lastRequestedSlot = new VisionUserPreference();
        lastRequestedSlot.setUser(user);
        lastRequestedSlot.setPreferenceKey("last_requested_slot");
        lastRequestedSlot.setPreferenceValue("scheduled_time");
        lastRequestedSlot.setObservationCount(3);
        lastRequestedSlot.setConfidenceScore(0.89d);
        lastRequestedSlot.setLastObservedAt(Instant.parse("2026-07-03T09:00:00Z"));

        VisionMemoryFeedbackEvent feedbackEvent = new VisionMemoryFeedbackEvent();
        feedbackEvent.setUser(user);
        feedbackEvent.setFeedbackType(VisionMemoryFeedbackType.CORRECTION);
        feedbackEvent.setCreatedAt(Instant.parse("2026-07-03T10:00:00Z"));

        AtomicReference<VisionMemorySummary> savedSummary = new AtomicReference<>();
        when(preferenceRepository.findByUser(user)).thenReturn(List.of(recentFeedback, lastIntent, lastRequestedSlot, preferredEntity, preferredInput));
        when(feedbackRepository.findTop20ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(feedbackEvent));
        when(summaryRepository.save(any(VisionMemorySummary.class))).thenAnswer(invocation -> {
            VisionMemorySummary summary = invocation.getArgument(0);
            savedSummary.set(summary);
            return summary;
        });
        when(summaryRepository.findTopByUserOrderByCreatedAtDesc(user)).thenAnswer(invocation -> Optional.ofNullable(savedSummary.get()));

        service.compactMemoryForUser(user);

        verify(summaryRepository).save(any(VisionMemorySummary.class));
        String summaryText = service.latestSummaryText(user);
        assertNotNull(summaryText);
        assertTrue(summaryText.contains("Top preferences"));
        assertTrue(summaryText.contains("Recent feedback"));
        var learningMemory = service.buildLearningMemory(user);
        assertNotNull(learningMemory);
        assertEquals("preferred_input_type", learningMemory.getPreferenceSignals().get(0).getPreferenceKey());
        assertEquals("voice", learningMemory.getPreferenceSignals().get(0).getPreferenceValue());
        assertEquals(5, learningMemory.getExplainabilityRecords().size());
        assertEquals("habit_selection", learningMemory.getExplainabilityRecords().get(0).getDecisionType());
        assertEquals("route_selection", learningMemory.getExplainabilityRecords().get(1).getDecisionType());
        assertEquals("intent_selection", learningMemory.getExplainabilityRecords().get(2).getDecisionType());
        assertEquals("slot_focus", learningMemory.getExplainabilityRecords().get(3).getDecisionType());
        assertEquals("feedback_signal", learningMemory.getExplainabilityRecords().get(4).getDecisionType());
    }

    @Test
    void confidenceDecayAndLearningMemorySnapshotStayStructured() {
        VisionUserPreferenceRepository preferenceRepository = mock(VisionUserPreferenceRepository.class);
        VisionMemoryFeedbackEventRepository feedbackRepository = mock(VisionMemoryFeedbackEventRepository.class);
        VisionMemorySummaryRepository summaryRepository = mock(VisionMemorySummaryRepository.class);
        VisionProperties visionProperties = new VisionProperties();

        VisionLearningService service = new VisionLearningService(
                preferenceRepository,
                feedbackRepository,
                summaryRepository,
                visionProperties
        );

        AppUser user = new AppUser();
        user.setId(8L);
        user.setUsername("assistant-user");

        VisionUserPreference preference = new VisionUserPreference();
        preference.setUser(user);
        preference.setPreferenceKey("preferred_input_type");
        preference.setPreferenceValue("voice");
        preference.setSourceType("runtime");
        preference.setObservationCount(4);
        preference.setConfidenceScore(0.90d);
        preference.setConfidenceUpdatedAt(Instant.parse("2026-06-01T00:00:00Z"));
        preference.setLastObservedAt(Instant.parse("2026-06-01T00:00:00Z"));

        when(preferenceRepository.findByUser(user)).thenReturn(List.of(preference));
        when(feedbackRepository.findTop20ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
        when(summaryRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.empty());

        double decayedConfidence = VisionPreferenceConfidenceSupport.decayConfidence(
                0.90d,
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-07-03T00:00:00Z"),
                visionProperties.getMemory()
        );

        assertTrue(decayedConfidence < 0.90d);
        assertTrue(decayedConfidence >= visionProperties.getMemory().getPreferenceConfidenceFloor());

        var memory = service.buildLearningMemory(user);
        assertNotNull(memory);
        assertEquals("voice", memory.getPreferenceSignals().get(0).getPreferenceValue());
        assertTrue(memory.getPreferenceSignals().get(0).getConfidenceScore() <= 0.90d);
    }
}
