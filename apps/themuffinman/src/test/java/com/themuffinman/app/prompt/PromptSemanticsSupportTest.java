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
    void classifiesDiscoveryPromptAndExtractsQuery() {
        PromptSemanticPlan plan = promptSemanticsSupport.inferPlan("show me open quests for moving help");

        assertEquals("DISCOVER_QUESTS", plan.getCandidateIntent());
        assertEquals("discover_quests", plan.getCapabilityId());
        assertEquals("moving help", plan.searchQueryOrEmpty());
    }
}
