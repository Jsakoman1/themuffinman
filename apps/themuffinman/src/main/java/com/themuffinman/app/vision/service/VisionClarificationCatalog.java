package com.themuffinman.app.vision.service;

import java.util.List;
import java.util.Map;

final class VisionClarificationCatalog {

    static final String SLOT_QUEST_TITLE = "quest_title";
    static final String SLOT_QUEST_DESCRIPTION = "quest_description";
    static final String SLOT_REWARD_AMOUNT = "reward_amount";
    static final String SLOT_VISIBILITY = "visibility";
    static final String SLOT_SCHEDULE_MODE = "schedule_mode";
    static final String SLOT_SCHEDULED_DATE = "scheduled_date";
    static final String SLOT_SCHEDULED_TIME = "scheduled_time";
    static final String SLOT_LOCATION_MODE = "location_mode";
    static final String SLOT_LOCATION_LABEL = "location_label";
    static final String SLOT_LOCATION_CANDIDATE_CONFIRMATION = "location_candidate_confirmation";
    static final String SLOT_TARGET_USER = "target_user";
    static final String SLOT_TARGET_APPLICATION_QUERY = "target_application_query";
    static final String SLOT_TARGET_QUEST_QUERY = "target_quest_query";
    static final String SLOT_APPLICATION_MESSAGE = "application_message";
    static final String SLOT_APPLICATION_PROPOSED_PRICE = "application_proposed_price";
    static final String SLOT_PROFILE_LOCATION_MODE = "profile_location_mode";
    static final String SLOT_PROFILE_LOCATION_LABEL = "profile_location_label";
    static final String SLOT_APPLICATION_UPDATE_FIELD = "application_update_field";
    static final String SLOT_TARGET_CIRCLE_QUERY = "target_circle_query";
    static final String SLOT_CIRCLE_NAME = "circle_name";

    private static final List<String> CREATE_QUEST_SLOT_ORDER = List.of(
            SLOT_QUEST_TITLE,
            SLOT_QUEST_DESCRIPTION,
            SLOT_REWARD_AMOUNT,
            SLOT_VISIBILITY,
            SLOT_SCHEDULE_MODE,
            SLOT_SCHEDULED_DATE,
            SLOT_SCHEDULED_TIME,
            SLOT_LOCATION_MODE,
            SLOT_LOCATION_LABEL,
            SLOT_LOCATION_CANDIDATE_CONFIRMATION
    );
    private static final List<String> CREATE_APPLICATION_SLOT_ORDER = List.of(
            SLOT_TARGET_QUEST_QUERY,
            SLOT_APPLICATION_MESSAGE,
            SLOT_APPLICATION_PROPOSED_PRICE
    );
    private static final List<String> UPDATE_APPLICATION_SLOT_ORDER = List.of(
            SLOT_TARGET_QUEST_QUERY,
            SLOT_APPLICATION_MESSAGE,
            SLOT_APPLICATION_PROPOSED_PRICE
    );
    private static final List<String> APPROVE_DECLINE_APPLICATION_SLOT_ORDER = List.of(
            SLOT_TARGET_QUEST_QUERY,
            SLOT_TARGET_USER
    );
    private static final List<String> VIEW_APPLICATION_DETAIL_SLOT_ORDER = List.of(
            SLOT_TARGET_APPLICATION_QUERY
    );
    private static final List<String> UPDATE_CIRCLE_SLOT_ORDER = List.of(
            SLOT_TARGET_CIRCLE_QUERY,
            SLOT_CIRCLE_NAME
    );

    private static final Map<String, String> QUESTION_BY_SLOT = Map.ofEntries(
            Map.entry(SLOT_QUEST_TITLE, "What should the quest be called?"),
            Map.entry(SLOT_QUEST_DESCRIPTION, "Describe the task in one or two clear sentences."),
            Map.entry(SLOT_TARGET_CIRCLE_QUERY, "What circle should I use? Say the circle name or circle id."),
            Map.entry(SLOT_REWARD_AMOUNT, "What is the reward amount, or should this quest be free?"),
            Map.entry(SLOT_VISIBILITY, "Should this quest be public or visible only to your circles?"),
            Map.entry(SLOT_SCHEDULE_MODE, "Should this quest happen at a fixed time or be arranged by agreement?"),
            Map.entry(SLOT_SCHEDULED_DATE, "What day should I use? Example: 2026-07-03, tomorrow, or next Tuesday."),
            Map.entry(SLOT_SCHEDULED_TIME, "What time should I use on that day? Example: 14:30, 2 pm, or around noon."),
            Map.entry(SLOT_LOCATION_MODE, "Should I use your profile location, hide the location, or use a custom place?"),
            Map.entry(SLOT_LOCATION_LABEL, "What custom place or address should I use for this quest?"),
            Map.entry(SLOT_LOCATION_CANDIDATE_CONFIRMATION, "I found a more precise place match. Should I use the resolved place or keep the location exactly as you typed it?"),
            Map.entry(SLOT_TARGET_USER, "Who is the person I should use for this action?"),
            Map.entry(SLOT_TARGET_APPLICATION_QUERY, "What application should I open? Say the application id or the exact quest title."),
            Map.entry(SLOT_TARGET_QUEST_QUERY, "What quest should I apply to? Say the quest title or quest id."),
            Map.entry(SLOT_APPLICATION_MESSAGE, "What message should I send with your application?"),
            Map.entry(SLOT_APPLICATION_PROPOSED_PRICE, "What proposed price should I send for this paid quest?"),
            Map.entry(SLOT_PROFILE_LOCATION_MODE, "Should I turn your profile location off, keep it approximate, or keep it exact?"),
            Map.entry(SLOT_PROFILE_LOCATION_LABEL, "What location or address should I save on your profile?"),
            Map.entry(SLOT_APPLICATION_UPDATE_FIELD, "What should I change in your application? You can give a new message, a new price, or both.")
    );

    private static final Map<String, String> RETRY_QUESTION_BY_SLOT = Map.ofEntries(
            Map.entry(SLOT_REWARD_AMOUNT, "I still need a usable reward value. Tell me an amount like 20 euros, or say this quest should be free."),
            Map.entry(SLOT_VISIBILITY, "I still need the visibility. Say public or circles only."),
            Map.entry(SLOT_SCHEDULE_MODE, "I still need the schedule type. Say fixed time or by agreement."),
            Map.entry(SLOT_SCHEDULED_DATE, "I still need the day. Use a date like 2026-07-03, 03.07.2026, tomorrow, or next Tuesday."),
            Map.entry(SLOT_SCHEDULED_TIME, "I still need the time. Use a format like 14:30, 2 pm, noon, or this evening."),
            Map.entry(SLOT_LOCATION_MODE, "I still need the location type. Say use profile, hide location, or custom place."),
            Map.entry(SLOT_LOCATION_LABEL, "I still need a real custom place or address, not just 'custom place'."),
            Map.entry(SLOT_LOCATION_CANDIDATE_CONFIRMATION, "Choose one: use resolved place, or keep typed location."),
            Map.entry(SLOT_TARGET_USER, "I still need one exact person target. Say a username, email, or name fragment."),
            Map.entry(SLOT_TARGET_APPLICATION_QUERY, "I still need one exact application target. Say the application id or the exact quest title."),
            Map.entry(SLOT_TARGET_QUEST_QUERY, "I still need one exact quest target. Say the exact quest title or quest id."),
            Map.entry(SLOT_TARGET_CIRCLE_QUERY, "I still need one exact circle target. Say the exact circle name or circle id."),
            Map.entry(SLOT_APPLICATION_MESSAGE, "I still need the application message you want to send."),
            Map.entry(SLOT_APPLICATION_PROPOSED_PRICE, "I still need a valid proposed price for this paid quest, for example 20 or 20.50."),
            Map.entry(SLOT_PROFILE_LOCATION_MODE, "I still need the profile location mode. Say off, approximate, or exact."),
            Map.entry(SLOT_PROFILE_LOCATION_LABEL, "I still need a usable profile location or address."),
            Map.entry(SLOT_APPLICATION_UPDATE_FIELD, "I still need at least one application change. Say a new message, a new price, or both.")
    );

    private VisionClarificationCatalog() {
    }

    static String nextMissingCreateQuestSlot(Map<String, String> slotData) {
        for (String slotId : CREATE_QUEST_SLOT_ORDER) {
            if (SLOT_REWARD_AMOUNT.equals(slotId)) {
                if (!slotData.containsKey(SLOT_REWARD_AMOUNT) && !slotData.containsKey("free_quest")) {
                    return slotId;
                }
                continue;
            }
            if (SLOT_SCHEDULED_DATE.equals(slotId) || SLOT_SCHEDULED_TIME.equals(slotId)) {
                if (!"fixed".equals(slotData.get(SLOT_SCHEDULE_MODE))) {
                    continue;
                }
            }
            if (SLOT_LOCATION_LABEL.equals(slotId)) {
                if (!"custom".equals(slotData.get(SLOT_LOCATION_MODE))) {
                    continue;
                }
            }
            if (SLOT_LOCATION_CANDIDATE_CONFIRMATION.equals(slotId)) {
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

    static String nextMissingCreateApplicationSlot(Map<String, String> slotData) {
        for (String slotId : CREATE_APPLICATION_SLOT_ORDER) {
            if (SLOT_APPLICATION_PROPOSED_PRICE.equals(slotId)) {
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

    static String nextMissingUpdateApplicationSlot(Map<String, String> slotData) {
        for (String slotId : UPDATE_APPLICATION_SLOT_ORDER) {
            if (SLOT_APPLICATION_PROPOSED_PRICE.equals(slotId)) {
                if (!"true".equals(slotData.get("application_price_required"))
                        || slotData.containsKey(SLOT_APPLICATION_PROPOSED_PRICE)
                        || !slotData.containsKey("application_existing_proposed_price")) {
                    continue;
                }
            }
            if (SLOT_APPLICATION_MESSAGE.equals(slotId)) {
                if (slotData.containsKey(SLOT_APPLICATION_MESSAGE) || slotData.containsKey(SLOT_APPLICATION_PROPOSED_PRICE)) {
                    continue;
                }
            }
            if (!slotData.containsKey(slotId)) {
                return slotId;
            }
        }
        return null;
    }

    static String nextMissingApproveDeclineApplicationSlot(Map<String, String> slotData) {
        for (String slotId : APPROVE_DECLINE_APPLICATION_SLOT_ORDER) {
            if (!slotData.containsKey(slotId)) {
                return slotId;
            }
        }
        return null;
    }

    static String nextMissingUpdateCircleSlot(Map<String, String> slotData) {
        for (String slotId : UPDATE_CIRCLE_SLOT_ORDER) {
            if (!slotData.containsKey(slotId)) {
                return slotId;
            }
        }
        return null;
    }

    static String nextMissingViewApplicationDetailSlot(Map<String, String> slotData) {
        for (String slotId : VIEW_APPLICATION_DETAIL_SLOT_ORDER) {
            if (!slotData.containsKey(slotId)) {
                return slotId;
            }
        }
        return null;
    }

    static String questionFor(String slotId) {
        return QUESTION_BY_SLOT.getOrDefault(slotId, "What is the next missing detail?");
    }

    static String retryQuestionFor(String slotId) {
        return RETRY_QUESTION_BY_SLOT.getOrDefault(slotId, questionFor(slotId));
    }
}
