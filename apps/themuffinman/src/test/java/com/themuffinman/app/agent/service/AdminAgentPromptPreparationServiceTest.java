package com.themuffinman.app.agent.service;

import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAgentPromptPreparationServiceTest {

    private final AdminAgentPromptPreparationService service =
            new AdminAgentPromptPreparationService(new PromptSemanticsSupport(), new com.themuffinman.app.vision.service.VisionSemanticRouteCatalogService());

    @Test
    void preparesPromptWithoutLocalTranslation() {
        AdminAgentPromptTranslation translation = service.preparePrompt("Generate 4 unique synthetic quests for user Josip about moving help");

        assertEquals("Generate 4 unique synthetic quests for user Josip about moving help", translation.getOriginalPrompt());
        assertEquals("Generate 4 unique synthetic quests for user Josip about moving help", translation.getTranslatedPrompt());
        assertEquals("none", translation.getTranslationProvider());
        assertFalse(translation.isTranslationApplied());
        assertTrue(translation.isTranslationReliable());
        assertEquals("Generate 4 unique synthetic quests for user Josip about moving help", translation.semanticEnvelopeOrEmpty().getRawUserText());
        assertEquals("Generate 4 unique synthetic quests for user Josip about moving help", translation.semanticEnvelopeOrEmpty().getNormalizedEnglishText());
        assertEquals(SemanticEntityFamily.QUEST, translation.semanticEnvelopeOrEmpty().getEntityFamily());
        assertEquals("CREATE_QUEST", translation.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("create_quest", translation.semanticPlanOrEmpty().getCapabilityId());
    }
}
