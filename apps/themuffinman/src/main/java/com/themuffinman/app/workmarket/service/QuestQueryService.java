package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.common.pagination.PageWindow;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class QuestQueryService {

    public PageWindow<Quest> buildQuestPage(
            List<Quest> sourceQuests,
            String query,
            QuestAudience audience,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean withImages,
            Boolean scheduledOnly,
            String sort,
            Integer page,
            Integer size,
            int defaultSize
    ) {
        String normalizedQuery = SearchQueryNormalizer.normalize(query).toLowerCase(Locale.ROOT);
        int safeSize = size == null || size < 1 ? defaultSize : size;
        int safePage = page == null || page < 0 ? 0 : page;

        List<Quest> quests = sourceQuests.stream()
                .filter(quest -> audience == null || quest.getAudience() == audience)
                .filter(quest -> matchesDateRange(quest, dateFrom, dateTo))
                .filter(quest -> withImages == null || !withImages || (quest.getImages() != null && !quest.getImages().isEmpty()))
                .filter(quest -> scheduledOnly == null || !scheduledOnly || quest.getScheduledAt() != null)
                .filter(quest -> matchesQuery(quest, normalizedQuery))
                .sorted(sortQuests(sort))
                .toList();

        return PageWindow.of(quests, safePage, safeSize);
    }

    private boolean matchesQuery(Quest quest, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        return safeLower(quest.getTitle()).contains(query)
                || safeLower(quest.getDescription()).contains(query)
                || safeLower(quest.getCreator().getUsername()).contains(query);
    }

    private boolean matchesDateRange(Quest quest, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null && dateTo == null) {
            return true;
        }

        if (quest.getScheduledAt() == null) {
            return false;
        }

        LocalDate questDate = quest.getScheduledAt().atZone(ZoneOffset.UTC).toLocalDate();
        if (dateFrom != null && questDate.isBefore(dateFrom)) {
            return false;
        }

        return dateTo == null || !questDate.isAfter(dateTo);
    }

    private Comparator<Quest> sortQuests(String sort) {
        String normalizedSort = sort == null ? "recommended" : sort.trim().toLowerCase(Locale.ROOT);

        return switch (normalizedSort) {
            case "newest" -> Comparator
                    .comparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Quest::getId, Comparator.reverseOrder());
            case "highest" -> Comparator
                    .comparing(Quest::getAwardAmount, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Quest::getId, Comparator.reverseOrder());
            default -> Comparator
                    .comparing(Quest::getAwardAmount, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Quest::getId, Comparator.reverseOrder());
        };
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
