package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.things.dto.ThingListingListResponseDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryItemDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestListPresetDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class VisionSearchDiscoveryService {

    private final WorkmarketQuestReadService questReadService;
    private final CircleReadService circleReadService;
    private final ThingSharingService thingSharingService;
    private final WorkmarketQuestApplicationReadService questApplicationReadService;
    private final AppUserRepository appUserRepository;
    private final SemanticAliasRegistry semanticAliasRegistry;

    public VisionSearchDiscoveryService(
            WorkmarketQuestReadService questReadService,
            CircleReadService circleReadService,
            ThingSharingService thingSharingService,
            WorkmarketQuestApplicationReadService questApplicationReadService,
            AppUserRepository appUserRepository,
            SemanticAliasRegistry semanticAliasRegistry
    ) {
        this.questReadService = questReadService;
        this.circleReadService = circleReadService;
        this.thingSharingService = thingSharingService;
        this.questApplicationReadService = questApplicationReadService;
        this.appUserRepository = appUserRepository;
        this.semanticAliasRegistry = semanticAliasRegistry;
    }

    public VisionSearchDiscoveryDTO discover(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            AppUser currentUser
    ) {
        if (currentUser == null || understanding == null) {
            return null;
        }

        String query = extractQuery(conversation, understanding);
        List<SearchCandidate> candidates = new ArrayList<>();
        candidates.addAll(discoverQuests(currentUser, query));
        candidates.addAll(discoverCircles(currentUser, query));
        candidates.addAll(discoverUsers(currentUser, query));
        candidates.addAll(discoverApplications(currentUser, query));
        candidates.addAll(discoverThings(currentUser, query));
        List<SearchCandidate> sortedCandidates = candidates.stream()
                .sorted(Comparator
                        .comparingInt(SearchCandidate::score).reversed()
                        .thenComparingInt(candidate -> familyPriority(candidate.item()))
                        .thenComparing(candidate -> candidate.item().getTitle() == null ? "" : candidate.item().getTitle(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        int page = discoveryPage(conversation);
        List<VisionSearchDiscoveryItemDTO> items = sortedCandidates.stream()
                .skip((long) page * 8)
                .limit(8)
                .map(SearchCandidate::item)
                .toList();

        String summary = buildSummary(query, items);
        return VisionSearchDiscoveryDTO.builder()
                .capabilityId("search")
                .query(query)
                .sort("relevance")
                .summary(summary)
                .totalItems(sortedCandidates.size())
                .hasMore(sortedCandidates.size() > (long) (page + 1) * 8)
                .items(items)
                .build();
    }

    private int discoveryPage(VisionConversation conversation) {
        if (conversation == null || conversation.getSlotData() == null) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(conversation.getSlotData().getOrDefault("search_page", "0")));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private List<SearchCandidate> discoverQuests(AppUser currentUser, String query) {
        String normalizedQuery = normalizeFamilyQuery(query, SemanticEntityFamily.QUEST);
        QuestListResponseDTO questList = questReadService.getQuestListPreset(
                QuestListPresetDTO.AVAILABLE,
                currentUser,
                normalizedQuery,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                "recommended",
                0,
                3
        );
        return questList.getItems().stream()
                .map(quest -> scoredItem(
                        "quest",
                        "discover_quests",
                        quest.getId(),
                        quest.getTitle(),
                        quest.getDescription(),
                        quest.getTitle(),
                        quest.getTitle(),
                        true,
                        normalizedQuery
                ))
                .toList();
    }

    private List<SearchCandidate> discoverCircles(AppUser currentUser, String query) {
        String normalizedQuery = normalizeFamilyQuery(query, SemanticEntityFamily.CIRCLE);
        return circleReadService.getCircles(currentUser).stream()
                .filter(circle -> matches(circle.getName(), circle.getMemberPreviewLabel(), normalizedQuery))
                .limit(3)
                .map(circle -> scoredItem(
                        "circle",
                        "view_circle_detail",
                        circle.getId(),
                        circle.getName(),
                        circle.getMemberCount() + " members" + (circle.getMemberPreviewLabel() == null || circle.getMemberPreviewLabel().isBlank()
                                ? ""
                                : " · " + circle.getMemberPreviewLabel()),
                        circle.getName(),
                        circle.getResolutionLabel(),
                        circle.isExactResolutionEligible(),
                        normalizedQuery
                ))
                .toList();
    }

    private List<SearchCandidate> discoverUsers(AppUser currentUser, String query) {
        String normalizedQuery = normalizeFamilyQuery(query, SemanticEntityFamily.USER);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }
        return appUserRepository.searchByUsernameOrEmail(normalizedQuery).stream()
                .filter(user -> currentUser == null || !currentUser.getId().equals(user.getId()))
                .limit(3)
                .map(user -> scoredItem(
                        "user",
                        "view_user_profile",
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getUsername(),
                        true,
                        normalizedQuery
                ))
                .toList();
    }

    private List<SearchCandidate> discoverApplications(AppUser currentUser, String query) {
        String normalizedQuery = normalizeFamilyQuery(query, SemanticEntityFamily.APPLICATION);
        return questApplicationReadService.getApplicationsForApplicant(currentUser).stream()
                .filter(application -> matches(application.getQuestTitle(), application.getMessage(), normalizedQuery))
                .limit(3)
                .map(application -> scoredItem(
                        "application",
                        "view_application_detail",
                        application.getId(),
                        application.getQuestTitle(),
                        application.getMessage(),
                        application.getQuestTitle(),
                        application.getQuestTitle(),
                        true,
                        normalizedQuery
                ))
                .toList();
    }

    private List<SearchCandidate> discoverThings(AppUser currentUser, String query) {
        String normalizedQuery = normalizeThingQuery(query);
        ThingListingListResponseDTO listings = thingSharingService.getAvailableListings(currentUser);
        List<ThingListingResponseDTO> availableItems = listings == null || listings.getItems() == null ? List.of() : listings.getItems();
        return availableItems.stream()
                .filter(listing -> matches(listing.getTitle(), listing.getDescription(), normalizedQuery))
                .limit(3)
                .map(listing -> scoredItem(
                        "thing",
                        "thing_listing",
                        listing.getId(),
                        listing.getTitle(),
                        listing.getDescription(),
                        listing.getTitle(),
                        listing.getOwnerUsername(),
                        true,
                        normalizedQuery
                ))
                .toList();
    }

    private SearchCandidate scoredItem(
            String entityFamily,
            String capabilityId,
            Long targetId,
            String title,
            String summary,
            String matchSummary,
            String resolutionLabel,
            boolean exactResolutionEligible,
            String query
    ) {
        return new SearchCandidate(
                VisionSearchDiscoveryItemDTO.builder()
                        .entityFamily(entityFamily)
                        .capabilityId(capabilityId)
                        .targetId(targetId)
                        .title(title)
                        .summary(summary)
                        .matchSummary(matchSummary == null ? null : "Matches " + matchSummary)
                        .resolutionLabel(resolutionLabel)
                        .exactResolutionEligible(exactResolutionEligible)
                        .build(),
                score(entityFamily, title, summary, matchSummary, exactResolutionEligible, query)
        );
    }

    private boolean matches(String left, String right, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalizedQuery = TextValueNormalizer.lowerTrimToEmpty(query);
        if (contains(left, normalizedQuery) || contains(right, normalizedQuery)) {
            return true;
        }

        for (String token : normalizedQuery.split("\\s+")) {
            if (token.length() < 3) {
                continue;
            }
            if (containsToken(left, token) || containsToken(right, token)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(String value, String query) {
        if (value == null || value.isBlank() || query == null || query.isBlank()) {
            return false;
        }
        return TextValueNormalizer.lowerTrimToEmpty(value).contains(query);
    }

    private boolean containsToken(String value, String token) {
        if (value == null || value.isBlank() || token == null || token.isBlank()) {
            return false;
        }
        return TextValueNormalizer.lowerTrimToEmpty(value).contains(token);
    }

    private int familyPriority(VisionSearchDiscoveryItemDTO item) {
        if (item == null || item.getEntityFamily() == null) {
            return 99;
        }
        return switch (item.getEntityFamily()) {
            case "quest" -> 0;
            case "circle" -> 1;
            case "user" -> 2;
            case "application" -> 3;
            case "thing" -> 4;
            default -> 5;
        };
    }

    private int score(
            String entityFamily,
            String title,
            String summary,
            String matchSummary,
            boolean exactResolutionEligible,
            String query
    ) {
        int score = switch (entityFamily) {
            case "quest" -> 500;
            case "circle" -> 400;
            case "user" -> 300;
            case "application" -> 200;
            case "thing" -> 100;
            default -> 0;
        };

        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery.isBlank()) {
            return score + (exactResolutionEligible ? 5 : 0);
        }

        score += relevanceBoost(title, normalizedQuery, 120, 40);
        score += relevanceBoost(summary, normalizedQuery, 60, 20);
        score += relevanceBoost(matchSummary, normalizedQuery, 30, 10);
        score += overlapBoost(title, normalizedQuery);
        score += overlapBoost(summary, normalizedQuery);
        if (exactResolutionEligible) {
            score += 5;
        }
        return score;
    }

    private int relevanceBoost(String value, String query, int exactBoost, int containsBoost) {
        if (value == null || value.isBlank() || query == null || query.isBlank()) {
            return 0;
        }
        String normalizedValue = TextValueNormalizer.lowerTrimToEmpty(value);
        if (normalizedValue.equals(query)) {
            return exactBoost;
        }
        if (normalizedValue.contains(query)) {
            return containsBoost;
        }
        return 0;
    }

    private int overlapBoost(String value, String query) {
        if (value == null || value.isBlank() || query == null || query.isBlank()) {
            return 0;
        }
        String[] queryTokens = TextValueNormalizer.lowerTrimToEmpty(query).split("\\s+");
        int overlap = 0;
        String normalizedValue = TextValueNormalizer.lowerTrimToEmpty(value);
        for (String token : queryTokens) {
            if (token.length() < 2) {
                continue;
            }
            if (normalizedValue.contains(token)) {
                overlap++;
            }
        }
        return overlap * 8;
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return TextValueNormalizer.lowerTrimToEmpty(query)
                .replaceAll("\\b(find|search|browse|discover|recommend|show|available|anything|something|me|for|please|look|look for|who|what|people|person|someone|anyone|can|could|help|help me|nearby|around)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeFamilyQuery(String query, SemanticEntityFamily family) {
        String normalized = normalizeQuery(query);
        if (normalized.isBlank() || family == null) {
            return normalized;
        }

        normalized = semanticAliasRegistry.normalizeQuery(family, normalized);
        List<String> familyTokens = switch (family) {
            case QUEST -> List.of("quest", "quests", "job", "jobs", "task", "tasks", "gig", "gigs", "work");
            case CIRCLE -> List.of("circle", "circles", "group", "groups", "team", "teams", "community", "communities", "crew", "crews");
            case USER -> List.of("user", "users", "profile", "profiles", "person", "people", "contact", "contacts", "member", "members", "account", "accounts");
            case APPLICATION -> List.of("application", "applications", "request", "requests", "submission", "submissions", "apply");
            default -> List.of();
        };
        for (String token : familyTokens) {
            normalized = normalized.replaceAll("\\b" + java.util.regex.Pattern.quote(token) + "\\b", " ");
        }
        return normalized.replaceAll("\\s+", " ").trim();
    }

    private String normalizeThingQuery(String query) {
        String normalized = normalizeQuery(query);
        if (normalized.isBlank()) {
            return normalized;
        }

        for (String token : List.of("thing", "things", "listing", "listings", "borrow", "borrowed", "share", "shared")) {
            normalized = normalized.replaceAll("\\b" + java.util.regex.Pattern.quote(token) + "\\b", " ");
        }
        return normalized.replaceAll("\\s+", " ").trim();
    }

    private String extractQuery(VisionConversation conversation, VisionPromptUnderstandingResult understanding) {
        String query = understanding.semanticPlanOrEmpty().searchQueryOrEmpty();
        if (query != null && !query.isBlank()) {
            return normalizeQuery(query);
        }
        if (conversation != null && conversation.getSlotData() != null) {
            String storedQuery = conversation.getSlotData().get("search_query");
            if (storedQuery != null && !storedQuery.isBlank()) {
                return normalizeQuery(storedQuery);
            }
        }
        String normalizedPrompt = understanding.getNormalizedPrompt();
        if (normalizedPrompt == null) {
            return "";
        }
        return normalizeQuery(normalizedPrompt);
    }

    private String buildSummary(String query, List<VisionSearchDiscoveryItemDTO> items) {
        if (query == null || query.isBlank()) {
            return items.isEmpty()
                    ? "No matches."
                    : "Showing matches across " + familySummary(items) + ". Say quests, circles, users, applications, or things to narrow it.";
        }
        String families = familySummary(items);
        boolean multipleFamilies = families.contains(",");
        if (items.isEmpty()) {
            return "No matches for \"" + query.trim() + "\".";
        }
        return multipleFamilies
                ? "Showing " + items.size() + " matches for \"" + query.trim() + "\" across " + families + ". Say one family to narrow it."
                : "Showing " + items.size() + " matches for \"" + query.trim() + "\" in " + families + ".";
    }

    private String familySummary(List<VisionSearchDiscoveryItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return "quests, circles, users, applications, and things";
        }

        List<String> families = new ArrayList<>();
        addFamilyLabel(families, items, "quest", "quests");
        addFamilyLabel(families, items, "circle", "circles");
        addFamilyLabel(families, items, "user", "users");
        addFamilyLabel(families, items, "application", "applications");
        addFamilyLabel(families, items, "thing", "things");
        return String.join(", ", families);
    }

    private void addFamilyLabel(List<String> families, List<VisionSearchDiscoveryItemDTO> items, String entityFamily, String label) {
        boolean present = items.stream().anyMatch(item -> entityFamily.equals(item.getEntityFamily()));
        if (present) {
            families.add(label);
        }
    }

    private record SearchCandidate(VisionSearchDiscoveryItemDTO item, int score) {
    }
}
