package com.themuffinman.app.vision.testing;

import java.util.LinkedHashMap;
import java.util.Map;

public final class VisionSlotStatePresets {

    private VisionSlotStatePresets() {
    }

    public static Map<String, String> createQuestBaseDetails() {
        Map<String, String> slotData = new LinkedHashMap<>();
        slotData.put("quest_title", "Garden cleanup");
        slotData.put("quest_description", "Need help cleaning up the garden.");
        slotData.put("reward_amount", "30");
        slotData.put("visibility", "CIRCLES");
        return slotData;
    }

    public static Map<String, String> createQuestAgreementCustomLocation() {
        Map<String, String> slotData = createQuestBaseDetails();
        slotData.put("schedule_mode", "agreement");
        slotData.put("location_mode", "custom");
        return slotData;
    }

    public static Map<String, String> createQuestFixedSchedule() {
        Map<String, String> slotData = createQuestBaseDetails();
        slotData.put("schedule_mode", "fixed");
        return slotData;
    }

    public static Map<String, String> createQuestReviewReadyProfileLocation() {
        Map<String, String> slotData = new LinkedHashMap<>();
        slotData.put("quest_title", "Help move a sofa");
        slotData.put("quest_description", "I need someone to help carry a sofa.");
        slotData.put("reward_amount", "20");
        slotData.put("visibility", "PUBLIC");
        slotData.put("schedule_mode", "agreement");
        slotData.put("location_mode", "profile");
        return slotData;
    }
}
