package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionLearningExplainabilityDTO;
import com.themuffinman.app.vision.model.VisionConversationAction;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.dto.VisionLearningMemoryDTO;
import com.themuffinman.app.vision.dto.VisionLearningPreferenceDTO;
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
import com.themuffinman.app.config.VisionProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@Service
public class VisionLearningService {

    private static final List<String> LEARNING_PREFERENCE_PRIORITY = List.of(
            "preferred_input_type",
            "preferred_language",
            "last_entity_family",
            "last_intent",
            "last_requested_slot",
            "last_feedback_type",
            "last_conversation_status"
    );
    private static final int EXPLAINABILITY_WINDOW = 7;

    private final VisionUserPreferenceRepository visionUserPreferenceRepository;
    private final VisionMemoryFeedbackEventRepository visionMemoryFeedbackEventRepository;
    private final VisionMemorySummaryRepository visionMemorySummaryRepository;
    private final VisionProperties visionProperties;

    public VisionLearningService(
            VisionUserPreferenceRepository visionUserPreferenceRepository,
            VisionMemoryFeedbackEventRepository visionMemoryFeedbackEventRepository,
            VisionMemorySummaryRepository visionMemorySummaryRepository,
            VisionProperties visionProperties
    ) {
        this.visionUserPreferenceRepository = visionUserPreferenceRepository;
        this.visionMemoryFeedbackEventRepository = visionMemoryFeedbackEventRepository;
        this.visionMemorySummaryRepository = visionMemorySummaryRepository;
        this.visionProperties = visionProperties;
    }

    @Transactional
    public void recordTurnOutcome(
            AppUser currentUser,
            VisionConversation conversation,
            VisionTurn turn,
            VisionPromptUnderstandingResult understanding,
            VisionSemanticRuntimeHints runtimeHints,
            VisionConversationAction action
    ) {
        if (currentUser == null || conversation == null || turn == null) {
            return;
        }

        VisionMemoryFeedbackType feedbackType = determineFeedbackType(conversation, turn, action);
        createFeedbackEvent(currentUser, conversation, turn, understanding, feedbackType);
        updatePreferences(currentUser, conversation, turn, understanding, runtimeHints, feedbackType);
    }

    @Transactional
    public void compactMemoryForUser(AppUser user) {
        if (user == null) {
            return;
        }

        List<VisionUserPreference> preferences = new ArrayList<>(visionUserPreferenceRepository.findByUser(user));
        preferences.sort(Comparator.comparingInt(VisionUserPreference::getObservationCount).reversed()
                .thenComparing(VisionUserPreference::getLastObservedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        List<VisionMemoryFeedbackEvent> feedbackEvents = visionMemoryFeedbackEventRepository.findTop20ByUserOrderByCreatedAtDesc(user);
        if (preferences.isEmpty() && feedbackEvents.isEmpty()) {
            return;
        }

        int summaryWindow = visionProperties.getMemory() == null ? 5 : visionProperties.getMemory().getSummaryWindow();
        int feedbackWindow = visionProperties.getMemory() == null ? 20 : visionProperties.getMemory().getRecentFeedbackWindow();
        String summaryText = buildSummaryText(preferences, feedbackEvents, summaryWindow, feedbackWindow);
        Instant now = Instant.now();
        VisionMemorySummary summary = new VisionMemorySummary();
        summary.setUser(user);
        summary.setSummaryKind(VisionMemorySummaryKind.ROLLUP);
        summary.setSummaryText(summaryText);
        summary.setSourceCount(preferences.size() + feedbackEvents.size());
        summary.setSourceWindowEndedAt(now);
        summary.setSourceWindowStartedAt(oldestTimestamp(preferences, feedbackEvents));
        visionMemorySummaryRepository.save(summary);
    }

    public String latestSummaryText(AppUser user) {
        if (user == null) {
            return null;
        }

        return visionMemorySummaryRepository.findTopByUserOrderByCreatedAtDesc(user)
                .map(VisionMemorySummary::getSummaryText)
                .orElse(null);
    }

    public List<String> recentFeedbackTypes(AppUser user) {
        if (user == null) {
            return List.of();
        }

        return visionMemoryFeedbackEventRepository.findTop20ByUserOrderByCreatedAtDesc(user).stream()
                .map(event -> event.getFeedbackType() == null ? null : event.getFeedbackType().name())
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(3)
                .toList();
    }

    public VisionLearningMemoryDTO buildLearningMemory(AppUser user) {
        if (user == null) {
            return null;
        }

        List<VisionLearningPreferenceDTO> preferenceSignals = buildPreferenceSignals(user);
        List<VisionLearningExplainabilityDTO> explainabilityRecords = buildExplainabilityRecords(user);
        String summaryText = latestSummaryText(user);
        List<String> feedbackTypes = recentFeedbackTypes(user);
        if (summaryText == null && feedbackTypes.isEmpty() && preferenceSignals.isEmpty() && explainabilityRecords.isEmpty()) {
            return null;
        }

        return VisionLearningMemoryDTO.builder()
                .summaryText(summaryText)
                .recentFeedbackTypes(feedbackTypes)
                .preferenceSignals(preferenceSignals)
                .explainabilityRecords(explainabilityRecords)
                .build();
    }

    private VisionMemoryFeedbackType determineFeedbackType(VisionConversation conversation, VisionTurn turn, VisionConversationAction action) {
        if (action == VisionConversationAction.REQUEST_REVIEW_EDIT) {
            return VisionMemoryFeedbackType.CORRECTION;
        }
        if (conversation.getStatus() != null && conversation.getStatus().name().equals("COMPLETED")) {
            return VisionMemoryFeedbackType.EXECUTED;
        }
        if (conversation.getStatus() != null && conversation.getStatus().name().equals("BLOCKED")) {
            return VisionMemoryFeedbackType.BLOCKED;
        }
        if (turn.getAgentState() != null && turn.getAgentState().name().equals("REVIEW_READY")) {
            return VisionMemoryFeedbackType.CONFIRMATION;
        }
        if (turn.getSource() == VisionTurnSource.VOICE) {
            return VisionMemoryFeedbackType.INTERACTION;
        }
        return VisionMemoryFeedbackType.INTERACTION;
    }

    private void createFeedbackEvent(
            AppUser currentUser,
            VisionConversation conversation,
            VisionTurn turn,
            VisionPromptUnderstandingResult understanding,
            VisionMemoryFeedbackType feedbackType
    ) {
        VisionMemoryFeedbackEvent event = new VisionMemoryFeedbackEvent();
        event.setUser(currentUser);
        event.setConversation(conversation);
        event.setTurn(turn);
        event.setFeedbackType(feedbackType);
        event.setIntent(turn.getDetectedIntent());
        event.setRequestedSlot(turn.getRequestedSlot());
        event.setPrompt(turn.getPrompt());
        event.setNormalizedPrompt(turn.getNormalizedPrompt());
        event.setAssistantMessage(turn.getAssistantMessage());
        event.setDetails(buildFeedbackDetails(conversation, turn, understanding, feedbackType));
        visionMemoryFeedbackEventRepository.save(event);
    }

    private void updatePreferences(
            AppUser currentUser,
            VisionConversation conversation,
            VisionTurn turn,
            VisionPromptUnderstandingResult understanding,
            VisionSemanticRuntimeHints runtimeHints,
            VisionMemoryFeedbackType feedbackType
    ) {
        Instant now = Instant.now();
        upsertPreference(currentUser, "preferred_input_type", normalize(runtimeHints == null ? null : runtimeHints.getInputType()), "runtime", now);
        upsertPreference(currentUser, "preferred_language", normalize(understanding == null ? null : understanding.getSourceLanguage()), "understanding", now);
        upsertPreference(currentUser, "last_intent", normalize(turn.getDetectedIntent() == null ? null : turn.getDetectedIntent().name()), "turn", now);
        upsertPreference(currentUser, "last_entity_family", normalize(VisionEntityFamilySupport.learningFamilyLabel(turn.getDetectedIntent())), "turn", now);
        upsertPreference(currentUser, "last_requested_slot", normalize(turn.getRequestedSlot()), "turn", now);
        upsertPreference(currentUser, "last_feedback_type", normalize(feedbackType.name()), "feedback", now);
        upsertPreference(currentUser, "last_conversation_status", normalize(conversation.getStatus() == null ? null : conversation.getStatus().name()), "conversation", now);
        if (understanding != null && understanding.isRepairAttempted() && understanding.getRepairSlotId() != null && !understanding.getRepairSlotId().isBlank()) {
            upsertPreference(currentUser, "last_repair_slot", normalize(understanding.getRepairSlotId()), "repair", now);
        }
    }

    private void upsertPreference(AppUser user, String preferenceKey, String preferenceValue, String sourceType, Instant observedAt) {
        if (user == null || preferenceKey == null || preferenceKey.isBlank() || preferenceValue == null || preferenceValue.isBlank()) {
            return;
        }

        VisionUserPreference preference = visionUserPreferenceRepository.findByUserAndPreferenceKey(user, preferenceKey)
                .orElseGet(VisionUserPreference::new);
        boolean isNew = preference.getId() == null;
        preference.setUser(user);
        preference.setPreferenceKey(preferenceKey);
        preference.setPreferenceValue(preferenceValue);
        preference.setSourceType(sourceType == null || sourceType.isBlank() ? "turn" : sourceType.trim());
        preference.setObservationCount(isNew ? 1 : preference.getObservationCount() + 1);
        preference.setConfidenceScore(VisionPreferenceConfidenceSupport.nextConfidence(preference, sourceType, observedAt, visionProperties.getMemory()));
        preference.setConfidenceUpdatedAt(observedAt);
        preference.setLastObservedAt(observedAt);
        if (!isNew) {
            preference.setUpdatedAt(observedAt);
        }
        visionUserPreferenceRepository.save(preference);
    }

    private String buildFeedbackDetails(
            VisionConversation conversation,
            VisionTurn turn,
            VisionPromptUnderstandingResult understanding,
            VisionMemoryFeedbackType feedbackType
    ) {
        List<String> parts = new ArrayList<>();
        if (feedbackType != null) {
            parts.add("type=" + feedbackType.name());
        }
        if (conversation.getIntent() != null) {
            parts.add("intent=" + conversation.getIntent().name());
        }
        if (turn.getRequestedSlot() != null && !turn.getRequestedSlot().isBlank()) {
            parts.add("requestedSlot=" + turn.getRequestedSlot());
        }
        if (turn.getSource() != null) {
            parts.add("source=" + turn.getSource().name().toLowerCase(Locale.ROOT));
        }
        if (turn.getNormalizedPrompt() != null && !turn.getNormalizedPrompt().isBlank()) {
            parts.add("normalizedPrompt=" + turn.getNormalizedPrompt());
        }
        if (turn.getDetectedIntent() != null) {
            parts.add("detectedIntent=" + turn.getDetectedIntent().name());
        }
        if (understanding != null && understanding.isRepairAttempted()) {
            parts.add("repairAttempted=true");
            if (understanding.getRepairSlotId() != null && !understanding.getRepairSlotId().isBlank()) {
                parts.add("repairSlot=" + understanding.getRepairSlotId());
            }
            if (understanding.getRepairNote() != null && !understanding.getRepairNote().isBlank()) {
                parts.add("repairNote=" + understanding.getRepairNote());
            }
        }
        return String.join("; ", parts);
    }

    private String buildSummaryText(
            List<VisionUserPreference> preferences,
            List<VisionMemoryFeedbackEvent> feedbackEvents,
            int summaryWindow,
            int feedbackWindow
    ) {
        List<String> summaryParts = new ArrayList<>();
        if (!preferences.isEmpty()) {
            Instant now = Instant.now();
            summaryParts.add("Top preferences: " + preferences.stream()
                    .sorted(Comparator.comparingDouble((VisionUserPreference preference) ->
                                    VisionPreferenceConfidenceSupport.effectiveConfidence(preference, now, visionProperties.getMemory()))
                            .reversed()
                            .thenComparing(VisionUserPreference::getObservationCount, Comparator.reverseOrder()))
                    .limit(Math.max(1, summaryWindow))
                    .map(preference -> preference.getPreferenceKey() + "=" + preference.getPreferenceValue()
                            + " [confidence="
                            + String.format(Locale.ROOT, "%.2f",
                            VisionPreferenceConfidenceSupport.effectiveConfidence(preference, now, visionProperties.getMemory()))
                            + "]")
                    .toList());
        }
        if (!feedbackEvents.isEmpty()) {
            summaryParts.add("Recent feedback: " + feedbackEvents.stream()
                    .limit(Math.max(1, feedbackWindow))
                    .map(event -> event.getFeedbackType() == null ? "unknown" : event.getFeedbackType().name().toLowerCase(Locale.ROOT))
                    .toList());
        }
        return String.join(" | ", summaryParts);
    }

    private List<VisionLearningPreferenceDTO> buildPreferenceSignals(AppUser user) {
        if (user == null) {
            return List.of();
        }

        List<VisionUserPreference> preferences = new ArrayList<>(visionUserPreferenceRepository.findByUser(user));
        if (preferences.isEmpty()) {
            return List.of();
        }

        int preferenceWindow = visionProperties.getMemory() == null ? 5 : visionProperties.getMemory().getSummaryWindow();
        return VisionPreferenceConfidenceSupport.toPreferenceSignals(
                preferences,
                Instant.now(),
                visionProperties.getMemory(),
                preferenceWindow,
                LEARNING_PREFERENCE_PRIORITY
        );
    }

    private List<VisionLearningExplainabilityDTO> buildExplainabilityRecords(AppUser user) {
        if (user == null) {
            return List.of();
        }

        List<VisionUserPreference> preferences = new ArrayList<>(visionUserPreferenceRepository.findByUser(user));
        if (preferences.isEmpty()) {
            return List.of();
        }

        int preferenceWindow = visionProperties.getMemory() == null ? 5 : visionProperties.getMemory().getSummaryWindow();
        int explainabilityWindow = Math.max(preferenceWindow, EXPLAINABILITY_WINDOW);
        List<VisionLearningPreferenceDTO> explainabilitySignals = VisionPreferenceConfidenceSupport.toPreferenceSignals(
                preferences,
                Instant.now(),
                visionProperties.getMemory(),
                explainabilityWindow,
                LEARNING_PREFERENCE_PRIORITY
        );
        return VisionPreferenceConfidenceSupport.toExplainabilityRecords(explainabilitySignals, explainabilityWindow);
    }

    private Instant oldestTimestamp(List<VisionUserPreference> preferences, List<VisionMemoryFeedbackEvent> feedbackEvents) {
        Instant oldest = Instant.now();
        for (VisionUserPreference preference : preferences) {
            if (preference.getLastObservedAt() != null && preference.getLastObservedAt().isBefore(oldest)) {
                oldest = preference.getLastObservedAt();
            }
        }
        for (VisionMemoryFeedbackEvent event : feedbackEvents) {
            if (event.getCreatedAt() != null && event.getCreatedAt().isBefore(oldest)) {
                oldest = event.getCreatedAt();
            }
        }
        return oldest;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

}
