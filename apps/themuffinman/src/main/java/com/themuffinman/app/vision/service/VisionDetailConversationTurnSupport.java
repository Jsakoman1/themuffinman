package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;

final class VisionDetailConversationTurnSupport {

    String resolveUserProfileQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.semanticPlanOrEmpty().getTargetUserQuery();
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_user".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            java.util.List.of(
                                    "show profile of",
                                    "open profile of",
                                    "show profile for",
                                    "open profile for",
                                    "show user",
                                    "open user"
                            )
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                java.util.List.of(
                        "show profile of",
                        "open profile of",
                        "show profile for",
                        "open profile for",
                        "show user",
                        "open user"
                )
        );
    }

    String resolveCircleDetailQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "target_circle_query")) {
            return semanticSlotValue(understanding, "target_circle_query").trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_circle_query".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            java.util.List.of("update circle", "rename circle", "delete circle", "remove circle"),
                            java.util.List.of(" to ")
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                java.util.List.of("update circle", "rename circle", "delete circle", "remove circle"),
                java.util.List.of(" to ")
        );
    }

    String resolveQuestDetailQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "target_quest_query")) {
            return semanticSlotValue(understanding, "target_quest_query").trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_quest_query".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            java.util.List.of("show quest details for", "show quest detail for", "show quest", "open quest")
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                java.util.List.of("show quest details for", "show quest detail for", "show quest", "open quest")
        );
    }

    String resolveApplicationDetailQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "target_application_query")) {
            return semanticSlotValue(understanding, "target_application_query").trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_application_query".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            java.util.List.of("show my application", "open my application", "show application", "open application", "application")
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                java.util.List.of("show my application", "open my application", "show application", "open application", "application")
        );
    }

    void applyResolvedApplicationTarget(
            VisionConversation conversation,
            String questQuery,
            VisionResolvedApplicationTarget target
    ) {
        conversation.getSlotData().put("target_quest_query", questQuery);
        conversation.getSlotData().put("application_quest_id", target.questId().toString());
        conversation.getSlotData().put("application_quest_title", target.questTitle());
        conversation.getSlotData().put("application_quest_creator", target.creatorUsername());
        conversation.getSlotData().put("application_price_required", Boolean.toString(target.priceRequired()));
        conversation.getSlotData().put("application_quest_reward_label", target.rewardLabel());
        conversation.getSlotData().put("application_existing_message", target.currentMessage() == null ? "" : target.currentMessage());
        conversation.getSlotData().put("application_existing_proposed_price", target.currentPrice() == null ? "" : target.currentPrice());
        if (target.applicationId() != null) {
            conversation.getSlotData().put("application_id", target.applicationId().toString());
        }
    }

    void applyResolvedQuestViewTarget(
            VisionConversation conversation,
            String questQuery,
            VisionResolvedQuestTarget target
    ) {
        conversation.getSlotData().put("target_quest_query", questQuery);
        conversation.getSlotData().put("resolved_quest_id", target.questId().toString());
        conversation.getSlotData().put("resolved_quest_title", target.questTitle());
        conversation.getSlotData().put("resolved_quest_creator", target.creatorUsername());
    }

    void applyResolvedCircleTarget(
            VisionConversation conversation,
            String circleQuery,
            VisionResolvedCircleTarget target
    ) {
        conversation.getSlotData().put("target_circle_query", circleQuery);
        conversation.getSlotData().put("resolved_circle_id", target.circleId().toString());
        conversation.getSlotData().put("resolved_circle_name", target.circleName());
        conversation.getSlotData().put("resolved_circle_member_count", target.memberCountLabel());
    }

    void applyResolvedProfileTarget(
            VisionConversation conversation,
            String targetUserQuery,
            VisionResolvedUserTarget target
    ) {
        conversation.getSlotData().put("target_user", targetUserQuery);
        conversation.getSlotData().put("resolved_profile_user_id", target.userId().toString());
        conversation.getSlotData().put("resolved_profile_username", target.username());
    }

    private boolean shouldUseSemanticSlotValue(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String slotId
    ) {
        String value = semanticSlotValue(understanding, slotId);
        Double confidence = semanticSlotConfidence(understanding, slotId);
        if (value == null || value.isBlank()) {
            return false;
        }
        if (confidence == null) {
            return true;
        }
        if (confidence >= VisionPromptUnderstandingResult.MIN_SLOT_CONFIDENCE) {
            return true;
        }
        return conversation != null && slotId.equals(conversation.getRequestedSlot());
    }

    private String semanticSlotValue(VisionPromptUnderstandingResult understanding, String slotId) {
        return understanding == null ? null : understanding.slotValue(slotId);
    }

    private Double semanticSlotConfidence(VisionPromptUnderstandingResult understanding, String slotId) {
        return understanding == null ? null : understanding.slotConfidence(slotId);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
