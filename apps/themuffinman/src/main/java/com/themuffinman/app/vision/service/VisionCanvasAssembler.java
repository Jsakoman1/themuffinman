package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionCanvasBlockDTO;
import com.themuffinman.app.vision.dto.VisionOptionDTO;
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
                .canvasMode(toCanvasMode(turn))
                .nextAction(turn.getNextAction().name())
                .message(turn.getAssistantMessage())
                .requestedSlot(turn.getRequestedSlot())
                .normalizedPrompt(turn.getNormalizedPrompt())
                .translationApplied(turn.isTranslationApplied())
                .translationReliable(turn.isTranslationReliable())
                .executionEnabled(visionProperties.isExecutionEnabled())
                .blocks(toBlocks(conversation, turn))
                .slotSummaries(toSlotSummaries(conversation.getSlotData()))
                .review(toReview(conversation.getSlotData(), turn))
                .build();
    }

    private String toCanvasMode(VisionTurn turn) {
        return switch (turn.getNextAction()) {
            case ASK_FOR_SLOT -> "clarification";
            case SHOW_REVIEW -> "review";
            case COMPLETE -> "complete";
            case BLOCKED -> "blocked";
        };
    }

    private List<VisionCanvasBlockDTO> toBlocks(VisionConversation conversation, VisionTurn turn) {
        List<VisionCanvasBlockDTO> blocks = new ArrayList<>();
        blocks.add(VisionCanvasBlockDTO.builder()
                .type("agent_message")
                .body(turn.getAssistantMessage())
                .tone(turn.getAgentState() == com.themuffinman.app.vision.model.VisionAgentState.BLOCKED ? "warning" : "neutral")
                .build());

        blocks.add(VisionCanvasBlockDTO.builder()
                .type("recognized_prompt")
                .title("Recognized input")
                .body(turn.getNormalizedPrompt())
                .build());

        if (!turn.isTranslationReliable()) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("warning")
                    .title("Translation check")
                    .body("Translation reliability dropped for this turn, so review the wording carefully.")
                    .tone("warning")
                    .build());
        }

        List<VisionSlotSummaryDTO> summaries = toSlotSummaries(conversation.getSlotData());
        if (!summaries.isEmpty()) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("result_summary")
                    .title("Collected so far")
                    .items(summaries)
                    .build());
        }

        if (turn.getRequestedSlot() != null && turn.getNextAction() == com.themuffinman.app.vision.model.VisionNextAction.ASK_FOR_SLOT) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("field_request")
                    .title(labelForSlot(turn.getRequestedSlot()))
                    .body(turn.getAssistantMessage())
                    .fieldId(turn.getRequestedSlot())
                    .fieldKind(kindForSlot(turn.getRequestedSlot()))
                    .required(true)
                    .placeholder(placeholderForSlot(turn.getRequestedSlot()))
                    .options(optionsForSlot(turn.getRequestedSlot()))
                    .build());
        }

        VisionQuestReviewDTO review = toReview(conversation.getSlotData(), turn);
        if (review != null) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("review_summary")
                    .title("Quest review")
                    .body(visionProperties.isExecutionEnabled()
                            ? "Review the collected quest data before confirmation."
                            : "Execution is still disabled, so this phase stops at review.")
                    .review(review)
                    .tone(visionProperties.isExecutionEnabled() ? "neutral" : "info")
                    .build());
        }

        if (turn.getNextAction() == com.themuffinman.app.vision.model.VisionNextAction.COMPLETE) {
            String createdQuestId = conversation.getSlotData().get("created_quest_id");
            String createdQuestTitle = conversation.getSlotData().get("quest_title");
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("success")
                    .title("Quest created")
                    .body(createdQuestId == null
                            ? "The quest was created successfully."
                            : "Created quest #" + createdQuestId + " for " + createdQuestTitle + ".")
                    .tone("success")
                    .build());
        }

        return blocks;
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

    private String labelForSlot(String slotId) {
        return switch (slotId) {
            case "quest_title" -> "Title";
            case "quest_description" -> "Description";
            case "reward_amount" -> "Reward";
            case "visibility" -> "Visibility";
            default -> "Next field";
        };
    }

    private String kindForSlot(String slotId) {
        return switch (slotId) {
            case "quest_description" -> "long_text";
            case "reward_amount" -> "money";
            case "visibility" -> "single_choice";
            default -> "short_text";
        };
    }

    private String placeholderForSlot(String slotId) {
        return switch (slotId) {
            case "quest_title" -> "Name the quest in a few words";
            case "quest_description" -> "Describe the task clearly";
            case "reward_amount" -> "Example: 20 euros or free";
            case "visibility" -> "Choose who should see the quest";
            default -> "Type the next detail";
        };
    }

    private List<VisionOptionDTO> optionsForSlot(String slotId) {
        if (!"visibility".equals(slotId)) {
            return List.of();
        }
        return List.of(
                VisionOptionDTO.builder().id("PUBLIC").label("Public").build(),
                VisionOptionDTO.builder().id("CIRCLES").label("Circles only").build()
        );
    }
}
