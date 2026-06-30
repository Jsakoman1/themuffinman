package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionQuestReviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionTurn;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VisionCanvasAssembler {

    private final VisionProperties visionProperties;

    public VisionCanvasAssembler(VisionProperties visionProperties) {
        this.visionProperties = visionProperties;
    }

    public VisionConversationTurnResponseDTO assemble(VisionConversation conversation, VisionTurn turn) {
        return VisionConversationTurnResponseDTO.builder()
                .conversationId(conversation.getId())
                .turnId(turn.getId())
                .intent(conversation.getIntent().name())
                .agentState(turn.getAgentState().name())
                .nextAction(turn.getNextAction().name())
                .message(turn.getAssistantMessage())
                .requestedSlot(turn.getRequestedSlot())
                .normalizedPrompt(turn.getNormalizedPrompt())
                .translationApplied(turn.isTranslationApplied())
                .translationReliable(turn.isTranslationReliable())
                .executionEnabled(visionProperties.isExecutionEnabled())
                .slotSummaries(toSlotSummaries(conversation.getSlotData()))
                .review(toReview(conversation.getSlotData(), turn))
                .build();
    }

    private List<VisionSlotSummaryDTO> toSlotSummaries(Map<String, String> slotData) {
        List<VisionSlotSummaryDTO> summaries = new ArrayList<>();
        addSummary(summaries, "quest_title", "Title", slotData.get("quest_title"));
        addSummary(summaries, "quest_description", "Description", slotData.get("quest_description"));
        if ("true".equals(slotData.get("free_quest"))) {
            addSummary(summaries, "reward_amount", "Reward", "Free");
        } else {
            addSummary(summaries, "reward_amount", "Reward", slotData.get("reward_amount"));
        }
        addSummary(summaries, "visibility", "Visibility", slotData.get("visibility"));
        return summaries;
    }

    private VisionQuestReviewDTO toReview(Map<String, String> slotData, VisionTurn turn) {
        if (!"SHOW_REVIEW".equals(turn.getNextAction().name())) {
            return null;
        }
        String rewardLabel = "true".equals(slotData.get("free_quest"))
                ? "Free"
                : slotData.get("reward_amount");
        return VisionQuestReviewDTO.builder()
                .title(slotData.get("quest_title"))
                .description(slotData.get("quest_description"))
                .rewardLabel(rewardLabel)
                .visibility(slotData.get("visibility"))
                .build();
    }

    private void addSummary(List<VisionSlotSummaryDTO> summaries, String slotId, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        summaries.add(VisionSlotSummaryDTO.builder()
                .slotId(slotId)
                .label(label)
                .value(value)
                .build());
    }
}
