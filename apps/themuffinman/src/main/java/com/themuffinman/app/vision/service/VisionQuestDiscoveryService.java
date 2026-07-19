package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryItemDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.workmarket.dto.QuestListPresetDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

@Service
public class VisionQuestDiscoveryService {

    private final WorkmarketQuestReadService questReadService;
    private final SemanticAliasRegistry semanticAliasRegistry;

    public VisionQuestDiscoveryService(WorkmarketQuestReadService questReadService, SemanticAliasRegistry semanticAliasRegistry) {
        this.questReadService = questReadService;
        this.semanticAliasRegistry = semanticAliasRegistry;
    }

    public VisionQuestDiscoveryDTO discover(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            AppUser currentUser
    ) {
        if (!isDiscoveryContext(conversation, understanding)) {
            return null;
        }

        String query = extractQuery(conversation, understanding);
        QuestListResponseDTO questList = questReadService.getQuestListPreset(
                QuestListPresetDTO.AVAILABLE,
                currentUser,
                query,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                "recommended",
                discoveryPage(conversation),
                5
        );

        List<VisionQuestDiscoveryItemDTO> items = IntStream.range(0, questList.getItems().size())
                .mapToObj(index -> toItem(questList.getItems().get(index), index + 1, query))
                .toList();

        String summary = items.isEmpty()
                ? buildEmptySummary(query)
                : buildSummary(query, questList.getTotalItems(), items.size());
        int page = discoveryPage(conversation);
        boolean blankQuery = query == null || query.isBlank();

        return VisionQuestDiscoveryDTO.builder()
                .capabilityId("discover_quests")
                .query(query)
                .sort("recommended")
                .summary(summary)
                .page(page)
                .pageSize(5)
                .resultState(items.isEmpty() ? (blankQuery ? "EMPTY_QUERY" : "NO_MATCHES") : "RESULTS")
                .recoveryAction(items.isEmpty() ? (blankQuery ? "ENTER_QUERY" : "REFINE_QUERY") : null)
                .totalItems(questList.getTotalItems())
                .hasMore(questList.getTotalItems() > (long) page * 5 + items.size())
                .items(items)
                .build();
    }

    private int discoveryPage(VisionConversation conversation) {
        if (conversation == null || conversation.getSlotData() == null) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(conversation.getSlotData().getOrDefault("discovery_page", "0")));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private boolean isDiscoveryContext(VisionConversation conversation, VisionPromptUnderstandingResult understanding) {
        if (conversation != null && conversation.getIntent() == VisionIntent.DISCOVER_QUESTS) {
            return true;
        }
        if (understanding == null) {
            return false;
        }
        return understanding.semanticPlanOrEmpty().candidateIntentOrUnsupported() == VisionIntent.DISCOVER_QUESTS;
    }

    private VisionQuestDiscoveryItemDTO toItem(QuestResponseDTO quest, int rank, String query) {
        return VisionQuestDiscoveryItemDTO.builder()
                .questId(quest.getId())
                .rank(rank)
                .title(quest.getTitle())
                .description(quest.getDescription())
                .creatorUsername(quest.getCreatorUsername())
                .rewardLabel(formatReward(quest))
                .statusLabel(quest.getPresentation() == null ? quest.getStatus().name() : quest.getPresentation().getStatusLabel())
                .locationLabel(quest.getPresentation() == null ? quest.getLocationLabel() : quest.getPresentation().getLocationLabel())
                .scheduledAt(quest.getScheduledAt())
                .matchSummary(buildMatchSummary(quest, query))
                .build();
    }

    private String buildMatchSummary(QuestResponseDTO quest, String query) {
        if (query == null || query.isBlank()) {
            return "Recommended from the available quests.";
        }
        return "Matches " + query.trim() + ".";
    }

    private String buildSummary(String query, long totalItems, int shownItems) {
        String scopeLabel = query == null || query.isBlank()
                ? "available quests"
                : "available quests matching \"" + query.trim() + "\"";
        return "Showing " + shownItems + " of " + totalItems + " " + scopeLabel + ".";
    }

    private String buildEmptySummary(String query) {
        if (query == null || query.isBlank()) {
            return "No open quests matched the current availability filter.";
        }
        return "No open quests matched \"" + query.trim() + "\".";
    }

    private String extractQuery(VisionConversation conversation, VisionPromptUnderstandingResult understanding) {
        String query = understanding == null ? "" : understanding.semanticPlanOrEmpty().searchQueryOrEmpty();
        if (!query.isBlank()) {
            return query.trim();
        }

        if (conversation != null) {
            String storedQuery = conversation.getSlotData().get("search_query");
            if (storedQuery != null && !storedQuery.isBlank()) {
                return storedQuery.trim();
            }
            String normalizedPrompt = conversation.getLastNormalizedPrompt();
            String extracted = extractQueryFromPrompt(normalizedPrompt);
            if (!extracted.isBlank()) {
                return extracted;
            }
        }

        String normalizedPrompt = understanding == null ? null : understanding.getNormalizedPrompt();
        return extractQueryFromPrompt(normalizedPrompt);
    }

    private String extractQueryFromPrompt(String normalizedPrompt) {
        if (normalizedPrompt == null) {
            return "";
        }
        return semanticAliasRegistry.normalizeQuery(SemanticEntityFamily.QUEST,
                        normalizedPrompt.replaceAll("(?i)\\b(find|browse|show|search|discover|recommend|open|available|quests?|jobs?|work|tasks?|for|near|around|me|please)\\b", " "))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String formatReward(QuestResponseDTO quest) {
        if (quest.getAwardAmount() == null || quest.getAwardAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Free";
        }
        return quest.getAwardAmount().stripTrailingZeros().toPlainString();
    }
}
