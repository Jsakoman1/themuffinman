package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.common.pagination.PageWindow;
import com.themuffinman.app.location.service.LocationGeoService;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestQueryService {
    private final LocationGeoService locationGeoService;

    public PageWindow<Quest> buildQuestPage(
            List<Quest> sourceQuests,
            String query,
            QuestAudience audience,
            LocalDate dateFrom,
            LocalDate dateTo,
            String viewerTimeZone,
            Integer viewerTimezoneOffsetMinutes,
            Boolean withImages,
            Boolean scheduledOnly,
            Integer radiusKm,
            BigDecimal originLatitude,
            BigDecimal originLongitude,
            String sort,
            Integer page,
            Integer size,
            int defaultSize
    ) {
        String normalizedQuery = SearchQueryNormalizer.normalize(query).toLowerCase(Locale.ROOT);
        int safeSize = size == null || size < 1 ? defaultSize : size;
        int safePage = page == null || page < 0 ? 0 : page;
        Instant now = Instant.now();

        List<Quest> quests = sourceQuests.stream()
                .filter(quest -> audience == null || quest.getAudience() == audience)
                .filter(quest -> matchesDateRange(quest, dateFrom, dateTo, viewerTimeZone, viewerTimezoneOffsetMinutes))
                .filter(quest -> withImages == null || !withImages || (quest.getImages() != null && !quest.getImages().isEmpty()))
                .filter(quest -> scheduledOnly == null || !scheduledOnly || quest.getScheduledAt() != null)
                .filter(quest -> matchesRadius(quest, radiusKm, originLatitude, originLongitude))
                .filter(quest -> matchesQuery(quest, normalizedQuery))
                .toList();

        Comparator<Quest> comparator = sortQuests(sort, quests, normalizedQuery, originLatitude, originLongitude, now);
        List<Quest> sortedQuests = quests.stream()
                .sorted(comparator)
                .toList();

        return PageWindow.of(sortedQuests, safePage, safeSize);
    }

    private boolean matchesQuery(Quest quest, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        return safeLower(quest.getTitle()).contains(query)
                || safeLower(quest.getDescription()).contains(query)
                || safeLower(quest.getCreator().getUsername()).contains(query);
    }

    private boolean matchesDateRange(
            Quest quest,
            LocalDate dateFrom,
            LocalDate dateTo,
            String viewerTimeZone,
            Integer viewerTimezoneOffsetMinutes
    ) {
        if (dateFrom == null && dateTo == null) {
            return true;
        }

        if (quest.getScheduledAt() == null) {
            return false;
        }

        ZoneId viewerZone = resolveViewerZone(viewerTimeZone, viewerTimezoneOffsetMinutes);
        LocalDate questDate = quest.getScheduledAt().atZone(viewerZone).toLocalDate();
        if (dateFrom != null && questDate.isBefore(dateFrom)) {
            return false;
        }

        return dateTo == null || !questDate.isAfter(dateTo);
    }

    private ZoneId resolveViewerZone(String viewerTimeZone, Integer viewerTimezoneOffsetMinutes) {
        if (viewerTimeZone != null && !viewerTimeZone.isBlank()) {
            try {
                return ZoneId.of(viewerTimeZone.trim());
            } catch (RuntimeException ignored) {
                // Fall through to offset or UTC fallback.
            }
        }

        if (viewerTimezoneOffsetMinutes == null) {
            return ZoneOffset.UTC;
        }

        return ZoneOffset.ofTotalSeconds(-viewerTimezoneOffsetMinutes * 60);
    }

    private boolean matchesRadius(Quest quest, Integer radiusKm, BigDecimal originLatitude, BigDecimal originLongitude) {
        if (radiusKm == null) {
            return true;
        }
        if (originLatitude == null || originLongitude == null) {
            return false;
        }
        if (!locationGeoService.isQuestSearchable(quest)) {
            return false;
        }
        if (!isWithinBoundingBox(quest, radiusKm, originLatitude, originLongitude)) {
            return false;
        }

        return locationGeoService.distanceKm(
                originLatitude,
                originLongitude,
                quest.getLocationLatitude(),
                quest.getLocationLongitude()
        ) <= radiusKm;
    }

    private boolean isWithinBoundingBox(Quest quest, Integer radiusKm, BigDecimal originLatitude, BigDecimal originLongitude) {
        double latitudeDelta = radiusKm / 111.32d;
        double longitudeDivisor = Math.max(0.1d, Math.cos(Math.toRadians(originLatitude.doubleValue())) * 111.32d);
        double longitudeDelta = radiusKm / longitudeDivisor;

        double questLatitude = quest.getLocationLatitude().doubleValue();
        double questLongitude = quest.getLocationLongitude().doubleValue();
        double originLat = originLatitude.doubleValue();
        double originLng = originLongitude.doubleValue();

        return questLatitude >= originLat - latitudeDelta
                && questLatitude <= originLat + latitudeDelta
                && questLongitude >= originLng - longitudeDelta
                && questLongitude <= originLng + longitudeDelta;
    }

    private Comparator<Quest> sortQuests(
            String sort,
            List<Quest> quests,
            String normalizedQuery,
            BigDecimal originLatitude,
            BigDecimal originLongitude,
            Instant now
    ) {
        String normalizedSort = sort == null ? "recommended" : sort.trim().toLowerCase(Locale.ROOT);

        return switch (normalizedSort) {
            case "newest" -> Comparator
                    .comparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Quest::getId, Comparator.reverseOrder());
            case "highest" -> Comparator
                    .comparing(Quest::getAwardAmount, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Quest::getId, Comparator.reverseOrder());
            default -> recommendedComparator(quests, normalizedQuery, originLatitude, originLongitude, now);
        };
    }

    private Comparator<Quest> recommendedComparator(
            List<Quest> quests,
            String normalizedQuery,
            BigDecimal originLatitude,
            BigDecimal originLongitude,
            Instant now
    ) {
        Map<Long, Integer> scores = quests.stream()
                .collect(Collectors.toMap(Quest::getId, quest -> recommendedScore(quest, normalizedQuery, originLatitude, originLongitude, now)));

        return Comparator
                .comparingInt((Quest quest) -> scores.getOrDefault(quest.getId(), 0))
                .reversed()
                .thenComparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Quest::getAwardAmount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Quest::getId, Comparator.reverseOrder());
    }

    private int recommendedScore(
            Quest quest,
            String normalizedQuery,
            BigDecimal originLatitude,
            BigDecimal originLongitude,
            Instant now
    ) {
        int score = 0;

        score += queryRelevanceScore(quest, normalizedQuery);
        score += distanceScore(quest, originLatitude, originLongitude);
        score += scheduleScore(quest, now);
        score += quest.getImages() != null && !quest.getImages().isEmpty() ? 12 : 0;
        score += awardScore(quest);
        score += quest.isTermFixed() ? 4 : 8;

        return score;
    }

    private int queryRelevanceScore(Quest quest, String normalizedQuery) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return 0;
        }

        String title = safeLower(quest.getTitle());
        String description = safeLower(quest.getDescription());
        String creator = safeLower(quest.getCreator() == null ? null : quest.getCreator().getUsername());

        int score = 0;
        if (title.equals(normalizedQuery)) {
            score += 180;
        } else if (title.startsWith(normalizedQuery)) {
            score += 140;
        } else if (title.contains(normalizedQuery)) {
            score += 100;
        }

        if (description.contains(normalizedQuery)) {
            score += 40;
        }
        if (creator.contains(normalizedQuery)) {
            score += 24;
        }

        return score;
    }

    private int distanceScore(Quest quest, BigDecimal originLatitude, BigDecimal originLongitude) {
        if (originLatitude == null || originLongitude == null || !locationGeoService.isQuestSearchable(quest)) {
            return 0;
        }

        double distanceKm = locationGeoService.distanceKm(
                originLatitude,
                originLongitude,
                quest.getLocationLatitude(),
                quest.getLocationLongitude()
        );

        if (distanceKm <= 1) {
            return 90;
        }
        if (distanceKm <= 3) {
            return 72;
        }
        if (distanceKm <= 10) {
            return 54;
        }
        if (distanceKm <= 20) {
            return 32;
        }
        if (distanceKm <= 30) {
            return 18;
        }
        return 6;
    }

    private int scheduleScore(Quest quest, Instant now) {
        if (quest.getScheduledAt() == null) {
            return 18;
        }

        if (quest.getScheduledAt().isBefore(now)) {
            return 0;
        }

        long hoursUntilStart = java.time.Duration.between(now, quest.getScheduledAt()).toHours();
        if (hoursUntilStart <= 24) {
            return 52;
        }
        if (hoursUntilStart <= 72) {
            return 38;
        }
        if (hoursUntilStart <= 24 * 7) {
            return 24;
        }
        if (hoursUntilStart <= 24 * 30) {
            return 10;
        }
        return 4;
    }

    private int awardScore(Quest quest) {
        if (quest.getAwardAmount() == null) {
            return 0;
        }

        double amount = quest.getAwardAmount().doubleValue();
        return (int) Math.min(36, Math.round(Math.min(amount, 180d) / 5d));
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
