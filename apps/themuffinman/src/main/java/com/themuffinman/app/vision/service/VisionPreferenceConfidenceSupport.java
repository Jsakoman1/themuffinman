package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.dto.VisionLearningExplainabilityDTO;
import com.themuffinman.app.vision.dto.VisionLearningPreferenceDTO;
import com.themuffinman.app.vision.model.VisionUserPreference;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

final class VisionPreferenceConfidenceSupport {

    private static final double DEFAULT_FLOOR = 0.12d;
    private static final double DEFAULT_THRESHOLD = 0.30d;
    private static final int DEFAULT_HALF_LIFE_HOURS = 168;

    private VisionPreferenceConfidenceSupport() {
    }

    static List<VisionLearningPreferenceDTO> toPreferenceSignals(
            List<VisionUserPreference> preferences,
            Instant now,
            VisionProperties.Memory memory,
            int limit
    ) {
        return toPreferenceSignals(preferences, now, memory, limit, List.of());
    }

    static List<VisionLearningPreferenceDTO> toPreferenceSignals(
            List<VisionUserPreference> preferences,
            Instant now,
            VisionProperties.Memory memory,
            int limit,
            List<String> priorityKeys
    ) {
        if (preferences == null || preferences.isEmpty()) {
            return List.of();
        }

        Instant effectiveNow = now == null ? Instant.now() : now;
        int safeLimit = Math.max(1, limit);
        return preferences.stream()
                .filter(preference -> preference != null && preference.getPreferenceKey() != null && !preference.getPreferenceKey().isBlank())
                .map(preference -> toPreferenceSignal(preference, effectiveNow, memory))
                .sorted(Comparator
                        .comparingInt((VisionLearningPreferenceDTO preference) -> priorityRank(preference.getPreferenceKey(), priorityKeys))
                        .thenComparing(Comparator.comparingDouble(VisionLearningPreferenceDTO::getConfidenceScore).reversed())
                        .thenComparing(VisionLearningPreferenceDTO::getObservationCount, Comparator.reverseOrder())
                        .thenComparing(VisionLearningPreferenceDTO::getLastObservedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .toList();
    }

    static List<VisionLearningExplainabilityDTO> toExplainabilityRecords(
            List<VisionLearningPreferenceDTO> preferenceSignals,
            int limit
    ) {
        if (preferenceSignals == null || preferenceSignals.isEmpty()) {
            return List.of();
        }

        int safeLimit = Math.max(1, limit);
        int upperBound = Math.min(safeLimit, preferenceSignals.size());
        return IntStream.range(0, upperBound)
                .mapToObj(index -> toExplainabilityRecord(preferenceSignals.get(index), index + 1))
                .toList();
    }

    static VisionLearningPreferenceDTO toPreferenceSignal(
            VisionUserPreference preference,
            Instant now,
            VisionProperties.Memory memory
    ) {
        if (preference == null) {
            return null;
        }

        Instant effectiveNow = now == null ? Instant.now() : now;
        double confidence = effectiveConfidence(preference, effectiveNow, memory);
        return VisionLearningPreferenceDTO.builder()
                .preferenceKey(clean(preference.getPreferenceKey()))
                .preferenceValue(clean(preference.getPreferenceValue()))
                .sourceType(clean(preference.getSourceType()))
                .observationCount(preference.getObservationCount())
                .confidenceScore(confidence)
                .lastObservedAt(preference.getLastObservedAt())
                .build();
    }

    static double nextConfidence(
            VisionUserPreference preference,
            String sourceType,
            Instant now,
            VisionProperties.Memory memory
    ) {
        Instant effectiveNow = now == null ? Instant.now() : now;
        double decayedConfidence = preference == null
                ? baseConfidenceForSource(sourceType)
                : effectiveConfidence(preference, effectiveNow, memory);
        double sourceWeight = baseConfidenceForSource(sourceType);
        double reinforcement = 0.10d + (sourceWeight * 0.12d);
        if (preference != null) {
            reinforcement += Math.min(0.05d, 0.01d * Math.max(0, preference.getObservationCount()));
        }
        return clamp(Math.max(decayedConfidence, decayedConfidence + reinforcement));
    }

    static double effectiveConfidence(VisionUserPreference preference, Instant now, VisionProperties.Memory memory) {
        if (preference == null) {
            return 0.0d;
        }

        double storedConfidence = preference.getConfidenceScore() == null
                ? baseConfidenceForSource(preference.getSourceType())
                : preference.getConfidenceScore();
        Instant anchor = preference.getConfidenceUpdatedAt() != null
                ? preference.getConfidenceUpdatedAt()
                : preference.getLastObservedAt();
        return decayConfidence(storedConfidence, anchor, now, memory);
    }

    static double decayConfidence(double confidence, Instant anchor, Instant now, VisionProperties.Memory memory) {
        double floor = floor(memory);
        double safeConfidence = clamp(confidence);
        if (anchor == null || now == null || !anchor.isBefore(now)) {
            return Math.max(floor, safeConfidence);
        }

        int halfLifeHours = halfLifeHours(memory);
        double elapsedHours = Duration.between(anchor, now).toMinutes() / 60.0d;
        if (elapsedHours <= 0.0d) {
            return Math.max(floor, safeConfidence);
        }

        double decayFactor = Math.pow(0.5d, elapsedHours / Math.max(1, halfLifeHours));
        return clamp(floor + ((safeConfidence - floor) * decayFactor));
    }

    static double threshold(VisionProperties.Memory memory) {
        if (memory == null) {
            return DEFAULT_THRESHOLD;
        }
        return clamp(memory.getPreferenceConfidenceThreshold());
    }

    static double floor(VisionProperties.Memory memory) {
        if (memory == null) {
            return DEFAULT_FLOOR;
        }
        return clamp(memory.getPreferenceConfidenceFloor());
    }

    static int halfLifeHours(VisionProperties.Memory memory) {
        if (memory == null || memory.getPreferenceConfidenceHalfLifeHours() <= 0) {
            return DEFAULT_HALF_LIFE_HOURS;
        }
        return memory.getPreferenceConfidenceHalfLifeHours();
    }

    static double baseConfidenceForSource(String sourceType) {
        String normalized = clean(sourceType);
        if (normalized == null) {
            return 0.60d;
        }
        return switch (normalized) {
            case "runtime" -> 0.72d;
            case "understanding" -> 0.68d;
            case "feedback" -> 0.66d;
            case "conversation" -> 0.62d;
            default -> 0.60d;
        };
    }

    private static int priorityRank(String preferenceKey, List<String> priorityKeys) {
        if (preferenceKey == null || priorityKeys == null || priorityKeys.isEmpty()) {
            return 0;
        }

        int index = priorityKeys.indexOf(preferenceKey);
        return index < 0 ? priorityKeys.size() : index;
    }

    private static double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0d;
        }
        return Math.max(0.0d, Math.min(1.0d, value));
    }

    private static VisionLearningExplainabilityDTO toExplainabilityRecord(
            VisionLearningPreferenceDTO preference,
            int rank
    ) {
        if (preference == null) {
            return null;
        }

        String key = clean(preference.getPreferenceKey());
        String value = clean(preference.getPreferenceValue());
        String decisionType = decisionTypeFor(key);
        return VisionLearningExplainabilityDTO.builder()
                .decisionType(decisionType)
                .preferenceKey(key)
                .preferenceValue(value)
                .rank(rank)
                .confidenceScore(preference.getConfidenceScore())
                .observationCount(preference.getObservationCount())
                .sourceType(clean(preference.getSourceType()))
                .reason(reasonFor(decisionType, key, value, rank, preference.getConfidenceScore(), preference.getObservationCount()))
                .build();
    }

    private static String decisionTypeFor(String preferenceKey) {
        if (preferenceKey == null || preferenceKey.isBlank()) {
            return "preference_ranking";
        }

        return switch (preferenceKey) {
            case "preferred_input_type" -> "habit_selection";
            case "preferred_language" -> "language_selection";
            case "last_entity_family" -> "route_selection";
            case "last_intent" -> "intent_selection";
            case "last_requested_slot" -> "slot_focus";
            case "last_feedback_type" -> "feedback_signal";
            case "last_conversation_status" -> "conversation_state";
            default -> "preference_ranking";
        };
    }

    private static String reasonFor(
            String decisionType,
            String preferenceKey,
            String preferenceValue,
            int rank,
            Double confidenceScore,
            int observationCount
    ) {
        String confidenceText = confidenceScore == null
                ? "unknown confidence"
                : String.format(Locale.ROOT, "%.2f", confidenceScore);
        String valueText = preferenceValue == null ? "unknown" : preferenceValue;
        return switch (decisionType) {
            case "habit_selection" -> "Rank #" + rank + " input habit signal; " + preferenceKey + "=" + valueText
                    + " stays visible because decay-aware confidence is " + confidenceText
                    + " after " + observationCount + " observations.";
            case "language_selection" -> "Rank #" + rank + " language habit signal; " + preferenceKey + "=" + valueText
                    + " remains useful because it was reinforced " + observationCount + " times.";
            case "route_selection" -> "Rank #" + rank + " route bias signal; " + preferenceKey + "=" + valueText
                    + " is the current entity-family hint at confidence " + confidenceText + ".";
            case "intent_selection" -> "Rank #" + rank + " intent memory signal; " + preferenceKey + "=" + valueText
                    + " keeps the thread anchored to the last reinforced intent.";
            case "slot_focus" -> "Rank #" + rank + " slot-focus signal; " + preferenceKey + "=" + valueText
                    + " matches the current requested-slot memory trail.";
            case "feedback_signal" -> "Rank #" + rank + " feedback signal; " + preferenceKey + "=" + valueText
                    + " helps explain recent corrections and confirmations.";
            case "conversation_state" -> "Rank #" + rank + " conversation-state signal; " + preferenceKey + "=" + valueText
                    + " records the last persisted turn outcome.";
            default -> "Rank #" + rank + " learned preference signal; " + preferenceKey + "=" + valueText
                    + " is sorted by backend priority and decay-aware confidence at " + confidenceText + ".";
        };
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
