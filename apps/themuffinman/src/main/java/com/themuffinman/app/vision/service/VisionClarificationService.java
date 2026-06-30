package com.themuffinman.app.vision.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VisionClarificationService {

    private static final List<String> CREATE_QUEST_SLOT_ORDER = List.of(
            "quest_title",
            "quest_description",
            "reward_amount",
            "visibility"
    );

    public String nextMissingCreateQuestSlot(Map<String, String> slotData) {
        for (String slotId : CREATE_QUEST_SLOT_ORDER) {
            if ("reward_amount".equals(slotId)) {
                if (!slotData.containsKey("reward_amount") && !slotData.containsKey("free_quest")) {
                    return slotId;
                }
                continue;
            }
            if (!slotData.containsKey(slotId)) {
                return slotId;
            }
        }
        return null;
    }

    public String buildQuestion(String slotId) {
        return switch (slotId) {
            case "quest_title" -> "What should the quest be called?";
            case "quest_description" -> "Describe the task in one or two clear sentences.";
            case "reward_amount" -> "What is the reward amount, or should this quest be free?";
            case "visibility" -> "Should this quest be public or visible only to your circles?";
            default -> "What is the next missing detail?";
        };
    }
}
