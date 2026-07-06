package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionDetailConversationTurnSupportTest {

    private final VisionDetailConversationTurnSupport support = new VisionDetailConversationTurnSupport();

    @Test
    void resolvesQueriesFromSemanticInputs() {
        VisionPromptUnderstandingSlots slots = new VisionPromptUnderstandingSlots();
        slots.setTargetCircleQuery("circle-42");
        slots.setTargetCircleQueryConfidence(0.9d);
        slots.setApplicationQuestQuery("quest-7");
        slots.setApplicationQuestQueryConfidence(0.9d);
        slots.setApplicationTargetQuery("application-9");
        slots.setApplicationTargetQueryConfidence(0.9d);

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.viewUserProfile(0.9d, "note", "alice"))
                .slots(slots)
                .build();

        VisionConversation circleConversation = new VisionConversation();
        circleConversation.setRequestedSlot("target_circle_query");
        VisionConversation questConversation = new VisionConversation();
        questConversation.setRequestedSlot("target_quest_query");
        VisionConversation applicationConversation = new VisionConversation();
        applicationConversation.setRequestedSlot("target_application_query");

        assertEquals("alice", support.resolveUserProfileQuery(new VisionConversation(), "show profile of bob", understanding));
        assertEquals("circle-42", support.resolveCircleDetailQuery(circleConversation, "show circle", understanding));
        assertEquals("quest-7", support.resolveQuestDetailQuery(questConversation, "show quest", understanding));
        assertEquals("application-9", support.resolveApplicationDetailQuery(applicationConversation, "show application", understanding));
    }

    @Test
    void appliesResolvedTargetsToConversationSlots() {
        VisionConversation conversation = new VisionConversation();
        conversation.setSlotData(new LinkedHashMap<>());

        support.applyResolvedProfileTarget(conversation, "alice", VisionResolvedUserTarget.resolved(1L, "alice"));
        support.applyResolvedCircleTarget(conversation, "circle-42", VisionResolvedCircleTarget.resolved(2L, "Circle 42", "12 members"));
        support.applyResolvedQuestViewTarget(conversation, "quest-7", VisionResolvedQuestTarget.resolved(3L, "Quest 7", "creator", true, "reward"));
        support.applyResolvedApplicationTarget(
                conversation,
                "quest-7",
                VisionResolvedApplicationTarget.resolved(3L, "Quest 7", "creator", true, "reward", "hello", "15", 9L)
        );

        assertEquals("alice", conversation.getSlotData().get("target_user"));
        assertEquals("1", conversation.getSlotData().get("resolved_profile_user_id"));
        assertEquals("alice", conversation.getSlotData().get("resolved_profile_username"));
        assertEquals("circle-42", conversation.getSlotData().get("target_circle_query"));
        assertEquals("2", conversation.getSlotData().get("resolved_circle_id"));
        assertEquals("Circle 42", conversation.getSlotData().get("resolved_circle_name"));
        assertEquals("12 members", conversation.getSlotData().get("resolved_circle_member_count"));
        assertEquals("quest-7", conversation.getSlotData().get("target_quest_query"));
        assertEquals("3", conversation.getSlotData().get("resolved_quest_id"));
        assertEquals("Quest 7", conversation.getSlotData().get("resolved_quest_title"));
        assertEquals("creator", conversation.getSlotData().get("resolved_quest_creator"));
        assertEquals("3", conversation.getSlotData().get("application_quest_id"));
        assertEquals("Quest 7", conversation.getSlotData().get("application_quest_title"));
        assertEquals("creator", conversation.getSlotData().get("application_quest_creator"));
        assertEquals("true", conversation.getSlotData().get("application_price_required"));
        assertEquals("reward", conversation.getSlotData().get("application_quest_reward_label"));
        assertEquals("hello", conversation.getSlotData().get("application_existing_message"));
        assertEquals("15", conversation.getSlotData().get("application_existing_proposed_price"));
        assertEquals("9", conversation.getSlotData().get("application_id"));
    }
}
