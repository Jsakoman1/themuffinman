package com.themuffinman.app.prompt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromptSemanticsSupportTest {

    private final PromptSemanticsSupport promptSemanticsSupport = new PromptSemanticsSupport();

    @Test
    void classifiesCreateQuestPrompt() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan("I need someone to move a sofa");

        assertEquals("CREATE_QUEST", plan.getCandidateIntent());
        assertEquals("create_quest", plan.getCapabilityId());
    }

    @Test
    void classifiesFurnitureMovingPrompt() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan(
                "i need somebody to help me mova my sofa in my apartment. maybe on weekend, saturday, in the evening, around 8 oclock ? i can pay 20 dollars."
        );

        assertEquals("CREATE_QUEST", plan.getCandidateIntent());
        assertEquals("create_quest", plan.getCapabilityId());
    }

    @Test
    void classifiesLongCreateQuestPrompt() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan("Create a paid quest called Move my sofa for tomorrow at 7 pm in Zurich for 20 euros");

        assertEquals("CREATE_QUEST", plan.getCandidateIntent());
        assertEquals("create_quest", plan.getCapabilityId());
    }

    @Test
    void classifiesCirclePrompt() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan("make a group called Neighbours");

        assertEquals("CREATE_CIRCLE", plan.getCandidateIntent());
        assertEquals("create_circle", plan.getCapabilityId());
    }

    @Test
    void classifiesApplicationPrompt() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan("submit application for quest 42");

        assertEquals("CREATE_APPLICATION", plan.getCandidateIntent());
        assertEquals("create_application", plan.getCapabilityId());
    }

    @Test
    void classifiesProfilePrompt() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan("edit profile and change bio");

        assertEquals("UPDATE_PROFILE", plan.getCandidateIntent());
        assertEquals("update_profile", plan.getCapabilityId());
    }

    @Test
    void classifiesChatPrompt() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan("send message to Josip");

        assertEquals("OPEN_CHAT", plan.getCandidateIntent());
        assertEquals("open_chat", plan.getCapabilityId());
    }

    @Test
    void classifiesDiscoveryPromptAndExtractsQuery() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan("show me open quests for moving help");

        assertEquals("DISCOVER_QUESTS", plan.getCandidateIntent());
        assertEquals("discover_quests", plan.getCapabilityId());
        assertEquals("moving help", plan.searchQueryOrEmpty());
    }
}
