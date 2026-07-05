package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionLearningMemoryDTO;
import com.themuffinman.app.vision.dto.VisionLearningPreferenceDTO;
import com.themuffinman.app.vision.model.VisionMemoryFeedbackEvent;
import com.themuffinman.app.vision.model.VisionMemoryFeedbackType;
import com.themuffinman.app.vision.model.VisionMemorySummary;
import com.themuffinman.app.vision.model.VisionMemorySummaryKind;
import com.themuffinman.app.vision.model.VisionUserPreference;
import com.themuffinman.app.vision.repository.VisionMemoryFeedbackEventRepository;
import com.themuffinman.app.vision.repository.VisionMemorySummaryRepository;
import com.themuffinman.app.vision.repository.VisionUserPreferenceRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionLearningEvaluationHarnessTest {

    @Test
    void learningRecallScenarioProducesCompactMemorySnapshot() {
        VisionUserPreferenceRepository preferenceRepository = mock(VisionUserPreferenceRepository.class);
        VisionMemoryFeedbackEventRepository feedbackRepository = mock(VisionMemoryFeedbackEventRepository.class);
        VisionMemorySummaryRepository summaryRepository = mock(VisionMemorySummaryRepository.class);
        VisionLearningService service = new VisionLearningService(preferenceRepository, feedbackRepository, summaryRepository, new VisionProperties());

        AppUser user = testUser(21L, "eval-user");
        VisionUserPreference preferredInput = preference(user, "preferred_input_type", "voice", 0.91d, Instant.parse("2026-07-01T00:00:00Z"));
        VisionUserPreference preferredFamily = preference(user, "last_entity_family", "quests", 0.78d, Instant.parse("2026-07-02T00:00:00Z"));
        VisionMemoryFeedbackEvent feedbackEvent = feedback(user, VisionMemoryFeedbackType.CORRECTION, Instant.parse("2026-07-03T00:00:00Z"));
        VisionMemorySummary summary = new VisionMemorySummary();
        summary.setUser(user);
        summary.setSummaryKind(VisionMemorySummaryKind.ROLLUP);
        summary.setSummaryText("Top preferences: [preferred_input_type=voice]");

        when(preferenceRepository.findByUser(user)).thenReturn(List.of(preferredInput, preferredFamily));
        when(feedbackRepository.findTop20ByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(feedbackEvent));
        when(summaryRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(summary));

        VisionLearningMemoryDTO memory = service.buildLearningMemory(user);

        assertNotNull(memory);
        assertEquals("Top preferences: [preferred_input_type=voice]", memory.getSummaryText());
        assertEquals(List.of("CORRECTION"), memory.getRecentFeedbackTypes());
        assertEquals(2, memory.getPreferenceSignals().size());
        assertEquals("preferred_input_type", memory.getPreferenceSignals().get(0).getPreferenceKey());
    }

    @Test
    void decayScenarioKeepsPreferenceConfidenceFlooredAndRanked() {
        VisionProperties.Memory memory = new VisionProperties.Memory();
        Instant anchor = Instant.parse("2026-06-01T00:00:00Z");
        Instant later = Instant.parse("2026-07-05T00:00:00Z");

        double decayed = VisionPreferenceConfidenceSupport.decayConfidence(0.91d, anchor, later, memory);

        assertTrue(decayed < 0.91d);
        assertTrue(decayed >= memory.getPreferenceConfidenceFloor());
    }

    @Test
    void preferenceAwareClarificationScenarioUsesVoiceFriendlyCopyWhenConfidenceIsStrong() {
        VisionClarificationService clarificationService = new VisionClarificationService();

        VisionSemanticUserMemoryContext voiceMemory = VisionSemanticUserMemoryContext.builder()
                .preferredInputType("voice")
                .preferredInputTypeConfidence(0.82d)
                .build();
        VisionSemanticUserMemoryContext weakMemory = VisionSemanticUserMemoryContext.builder()
                .preferredInputType("voice")
                .preferredInputTypeConfidence(0.10d)
                .build();

        assertTrue(clarificationService.buildCreateQuestConfidenceQuestion(voiceMemory).startsWith("Say"));
        assertTrue(clarificationService.buildCreateQuestConfidenceQuestion(weakMemory).startsWith("I can draft"));
    }

    private AppUser testUser(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private VisionUserPreference preference(AppUser user, String key, String value, double confidence, Instant updatedAt) {
        VisionUserPreference preference = new VisionUserPreference();
        preference.setUser(user);
        preference.setPreferenceKey(key);
        preference.setPreferenceValue(value);
        preference.setSourceType("turn");
        preference.setObservationCount(3);
        preference.setConfidenceScore(confidence);
        preference.setConfidenceUpdatedAt(updatedAt);
        preference.setLastObservedAt(updatedAt);
        return preference;
    }

    private VisionMemoryFeedbackEvent feedback(AppUser user, VisionMemoryFeedbackType type, Instant createdAt) {
        VisionMemoryFeedbackEvent event = new VisionMemoryFeedbackEvent();
        event.setUser(user);
        event.setFeedbackType(type);
        event.setCreatedAt(createdAt);
        return event;
    }
}
