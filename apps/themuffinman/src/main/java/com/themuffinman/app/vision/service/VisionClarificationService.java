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
            "scheduled_date",
            "scheduled_time",
            "location_mode",
            "location_label",
            "location_candidate_confirmation"
    );
    private static final List<String> CREATE_APPLICATION_SLOT_ORDER = List.of(
            "target_quest_query",
            "application_message",
            "application_proposed_price"
    );
    private static final List<String> UPDATE_APPLICATION_SLOT_ORDER = List.of(
            "target_quest_query",
            "application_message",
            "application_proposed_price"
    );
    private static final List<String> APPROVE_DECLINE_APPLICATION_SLOT_ORDER = List.of(
            "target_quest_query",
            "target_user"
    );
    private static final List<String> UPDATE_CIRCLE_SLOT_ORDER = List.of(
            "target_circle_query",
            "circle_name"
    );

    public String nextMissingCreateQuestSlot(Map<String, String> slotData) {
        for (String slotId : CREATE_QUEST_SLOT_ORDER) {
            if ("reward_amount".equals(slotId)) {
                if (!slotData.containsKey("reward_amount") && !slotData.containsKey("free_quest")) {
                    return slotId;
                }
                continue;
            }
            if ("scheduled_date".equals(slotId) || "scheduled_time".equals(slotId)) {
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

    public String nextMissingCreateApplicationSlot(Map<String, String> slotData) {
        for (String slotId : CREATE_APPLICATION_SLOT_ORDER) {
            if ("application_proposed_price".equals(slotId)) {
                if (!"true".equals(slotData.get("application_price_required"))) {
                    continue;
                }
            }
            if (!slotData.containsKey(slotId)) {
                return slotId;
            }
        }
        return null;
    }

    public String nextMissingUpdateApplicationSlot(Map<String, String> slotData) {
        for (String slotId : UPDATE_APPLICATION_SLOT_ORDER) {
            if ("application_proposed_price".equals(slotId)) {
                if (!"true".equals(slotData.get("application_price_required"))
                        || slotData.containsKey("application_proposed_price")
                        || !slotData.containsKey("application_existing_proposed_price")) {
                    continue;
                }
            }
            if ("application_message".equals(slotId)) {
                if (slotData.containsKey("application_message") || slotData.containsKey("application_proposed_price")) {
                    continue;
                }
            }
            if (!slotData.containsKey(slotId)) {
                return slotId;
            }
        }
        return null;
    }

    public String nextMissingApproveDeclineApplicationSlot(Map<String, String> slotData) {
        for (String slotId : APPROVE_DECLINE_APPLICATION_SLOT_ORDER) {
            if (!slotData.containsKey(slotId)) {
                return slotId;
            }
        }
        return null;
    }

    public String nextMissingUpdateCircleSlot(Map<String, String> slotData) {
        for (String slotId : UPDATE_CIRCLE_SLOT_ORDER) {
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
            case "target_circle_query" -> "What circle should I use? Say the circle name or circle id.";
            case "reward_amount" -> "What is the reward amount, or should this quest be free?";
            case "visibility" -> "Should this quest be public or visible only to your circles?";
            case "schedule_mode" -> "Should this quest happen at a fixed time or be arranged by agreement?";
            case "scheduled_date" -> "What day should I use? Example: 2026-07-03, tomorrow, or next Tuesday.";
            case "scheduled_time" -> "What time should I use on that day? Example: 14:30, 2 pm, or around noon.";
            case "location_mode" -> "Should I use your profile location, hide the location, or use a custom place?";
            case "location_label" -> "What custom place or address should I use for this quest?";
            case "location_candidate_confirmation" -> "I found a more precise place match. Should I use the resolved place or keep the location exactly as you typed it?";
            case "target_user" -> "Who is the person I should use for this action?";
            case "target_quest_query" -> "What quest should I apply to? Say the quest title or quest id.";
            case "application_message" -> "What message should I send with your application?";
            case "application_proposed_price" -> "What proposed price should I send for this paid quest?";
            case "profile_location_mode" -> "Should I turn your profile location off, keep it approximate, or keep it exact?";
            case "profile_location_label" -> "What location or address should I save on your profile?";
            case "application_update_field" -> "What should I change in your application? You can give a new message, a new price, or both.";
            default -> "What is the next missing detail?";
        };
    }

    public String buildRetryQuestion(String slotId) {
        return switch (slotId) {
            case "reward_amount" -> "I still need a usable reward value. Tell me an amount like 20 euros, or say this quest should be free.";
            case "visibility" -> "I still need the visibility. Say public or circles only.";
            case "schedule_mode" -> "I still need the schedule type. Say fixed time or by agreement.";
            case "scheduled_date" -> "I still need the day. Use a date like 2026-07-03, 03.07.2026, tomorrow, or next Tuesday.";
            case "scheduled_time" -> "I still need the time. Use a format like 14:30, 2 pm, noon, or this evening.";
            case "location_mode" -> "I still need the location type. Say use profile, hide location, or custom place.";
            case "location_label" -> "I still need a real custom place or address, not just 'custom place'.";
            case "location_candidate_confirmation" -> "Choose one: use resolved place, or keep typed location.";
            case "target_user" -> "I still need one exact person target. Say a username, email, or name fragment.";
            case "target_quest_query" -> "I still need one exact quest target. Say the exact quest title or quest id.";
            case "target_circle_query" -> "I still need one exact circle target. Say the exact circle name or circle id.";
            case "application_message" -> "I still need the application message you want to send.";
            case "application_proposed_price" -> "I still need a valid proposed price for this paid quest, for example 20 or 20.50.";
            case "profile_location_mode" -> "I still need the profile location mode. Say off, approximate, or exact.";
            case "profile_location_label" -> "I still need a usable profile location or address.";
            case "application_update_field" -> "I still need at least one application change. Say a new message, a new price, or both.";
            default -> buildQuestion(slotId);
        };
    }
}
