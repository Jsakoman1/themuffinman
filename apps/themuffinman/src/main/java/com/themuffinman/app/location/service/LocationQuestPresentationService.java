package com.themuffinman.app.location.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.workmarket.model.Quest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationQuestPresentationService {
    private final LocationAccessPolicyService locationAccessPolicyService;

    public String resolveQuestLocationLabel(Quest quest, AppUser viewer) {
        QuestLocationVisibility visibility = resolveEffectiveQuestLocationVisibility(quest);
        return switch (visibility) {
            case OFF -> null;
            case APPROXIMATE -> buildApproximateQuestLocationLabel(quest);
            case EXACT -> locationAccessPolicyService.canViewExactLocation(quest.getCreator(), viewer)
                    ? buildExactQuestLocationLabel(quest)
                    : buildApproximateQuestLocationLabel(quest);
            case INHERIT -> null;
        };
    }

    public String resolveQuestLocationSourceSummary(Quest quest) {
        return quest.getLocationSource() == QuestLocationSource.CUSTOM
                ? "Custom quest location"
                : "Uses creator profile location";
    }

    public String resolveQuestLocationVisibilitySummary(Quest quest, AppUser viewer) {
        QuestLocationVisibility visibility = resolveEffectiveQuestLocationVisibility(quest);
        return switch (visibility) {
            case OFF -> "Location hidden";
            case APPROXIMATE -> "Approximate area shown";
            case EXACT -> locationAccessPolicyService.canViewExactLocation(quest.getCreator(), viewer)
                    ? "Exact address shown"
                    : "Approximate area shown";
            case INHERIT -> "Location follows profile setting";
        };
    }

    private QuestLocationVisibility resolveEffectiveQuestLocationVisibility(Quest quest) {
        QuestLocationVisibility visibility = quest.getLocationVisibility() == null
                ? QuestLocationVisibility.INHERIT
                : quest.getLocationVisibility();

        if (visibility != QuestLocationVisibility.INHERIT) {
            return visibility;
        }

        UserLocationMode creatorMode = quest.getCreator() == null || quest.getCreator().getLocationMode() == null
                ? UserLocationMode.OFF
                : quest.getCreator().getLocationMode();

        return switch (creatorMode) {
            case OFF -> QuestLocationVisibility.OFF;
            case APPROXIMATE -> QuestLocationVisibility.APPROXIMATE;
            case EXACT -> QuestLocationVisibility.EXACT;
        };
    }

    private String buildApproximateQuestLocationLabel(Quest quest) {
        String locality = normalizeText(quest.getLocationLocality());
        String country = normalizeText(quest.getLocationCountry());
        if (locality != null && country != null) {
            return locality + ", " + country;
        }
        if (locality != null) {
            return locality;
        }
        if (country != null) {
            return country;
        }
        return normalizeText(quest.getLocationLabel());
    }

    private String buildExactQuestLocationLabel(Quest quest) {
        String street = normalizeText(quest.getLocationStreet());
        String houseNumber = normalizeText(quest.getLocationHouseNumber());
        String locality = normalizeText(quest.getLocationLocality());
        String country = normalizeText(quest.getLocationCountry());

        String primary = joinParts(" ", street, houseNumber);
        String secondary = joinParts(", ", locality, country);

        if (primary != null && secondary != null) {
            return primary + ", " + secondary;
        }
        if (primary != null) {
            return primary;
        }
        if (secondary != null) {
            return secondary;
        }
        return normalizeText(quest.getLocationLabel());
    }

    private String joinParts(String delimiter, String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            String normalized = normalizeText(part);
            if (normalized == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(delimiter);
            }
            builder.append(normalized);
        }
        return builder.isEmpty() ? null : builder.toString();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
