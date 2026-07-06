package com.themuffinman.app.vision.service;

import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.semantic.SemanticEnvelope;
import com.themuffinman.app.semantic.VisionEntityResolverRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionSemanticEnvelopeSupportTest {

    @Test
    void buildsSemanticEnvelopeForCreateQuestWithoutEntityResolution() {
        VisionSemanticEnvelopeSupport support = new VisionSemanticEnvelopeSupport(
                new VisionSemanticRouteCatalogService(),
                new VisionEntityResolverRegistry(List.of(), new SemanticAliasRegistry()),
                new SemanticAliasRegistry()
        );
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("move sofa")
                .semanticPlan(VisionSemanticPlan.createQuest(0.91d, "create quest"))
                .build();

        SemanticEnvelope envelope = support.buildSemanticEnvelope(
                "move sofa",
                understanding,
                null,
                "none",
                false,
                true,
                VisionPromptUnderstandingService.SEMANTIC_CONTRACT_VERSION,
                "vision-model"
        );

        assertEquals("move sofa", envelope.getRawUserText());
        assertEquals("move sofa", envelope.getNormalizedEnglishText());
        assertEquals("CREATE_QUEST", envelope.getIntent());
        assertEquals(SemanticEntityFamily.QUEST, envelope.getEntityFamily());
        assertEquals(0.91d, envelope.getConfidence());
    }
}
