package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.dto.VisionLearningPreferenceDTO;
import com.themuffinman.app.vision.model.VisionUserPreference;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }
}
