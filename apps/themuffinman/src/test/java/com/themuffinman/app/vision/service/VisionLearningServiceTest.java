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

        VisionMemoryFeedbackEvent feedbackEvent = new VisionMemoryFeedbackEvent();
        feedbackEvent.setUser(user);
        feedbackEvent.setFeedbackType(VisionMemoryFeedbackType.CORRECTION);
        feedbackEvent.setCreatedAt(Instant.parse("2026-07-03T10:00:00Z"));

        AtomicReference<VisionMemorySummary> savedSummary = new AtomicReference<>();
        when(preferenceRepository.findByUser(user)).thenReturn(List.of(preferredInput, preferredEntity));
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
        assertTrue(summaryText.contains("preferred_input_type=voice"));
        assertTrue(summaryText.contains("Recent feedback"));
    }
}
