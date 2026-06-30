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
            "visibility",
            "schedule_mode",
            "scheduled_at",
            "location_mode",
            "location_label",
            "location_candidate_confirmation"
    );

    public String nextMissingCreateQuestSlot(Map<String, String> slotData) {
        for (String slotId : CREATE_QUEST_SLOT_ORDER) {
            if ("reward_amount".equals(slotId)) {
                if (!slotData.containsKey("reward_amount") && !slotData.containsKey("free_quest")) {
                    return slotId;
                }
                continue;
            }
            if ("scheduled_at".equals(slotId)) {
                if (!"fixed".equals(slotData.get("schedule_mode"))) {
                    continue;
                }
            }
            if ("location_label".equals(slotId)) {
                if (!"custom".equals(slotData.get("location_mode"))) {
                    continue;
                }
            }
            if ("location_candidate_confirmation".equals(slotId)) {
                if (!"candidate_pending".equals(slotData.get("location_resolution_status"))) {
                    continue;
                }
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
            case "schedule_mode" -> "Should this quest happen at a fixed time or be arranged by agreement?";
            case "scheduled_at" -> "What exact date and time should I use? Example: 2026-07-03 14:30 or tomorrow at 14:30.";
            case "location_mode" -> "Should I use your profile location, hide the location, or use a custom place?";
            case "location_label" -> "What custom place or address should I use for this quest?";
            case "location_candidate_confirmation" -> "I found a more precise place match. Should I use the resolved place or keep the location exactly as you typed it?";
            default -> "What is the next missing detail?";
        };
    }

    public String buildRetryQuestion(String slotId) {
        return switch (slotId) {
            case "reward_amount" -> "I still need a usable reward value. Tell me an amount like 20 euros, or say this quest should be free.";
            case "visibility" -> "I still need the visibility. Say public or circles only.";
            case "schedule_mode" -> "I still need the schedule type. Say fixed time or by agreement.";
            case "scheduled_at" -> "I could not read the time yet. Use a format like 2026-07-03 14:30, 03.07.2026 14:30, or tomorrow at 14:30.";
            case "location_mode" -> "I still need the location type. Say use profile, hide location, or custom place.";
            case "location_label" -> "I still need a real custom place or address, not just 'custom place'.";
            case "location_candidate_confirmation" -> "Choose one: use resolved place, or keep typed location.";
            default -> buildQuestion(slotId);
        };
    }
}
