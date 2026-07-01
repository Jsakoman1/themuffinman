package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.service.LocalAdminAgentPromptTranslator;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionPromptUnderstandingServiceTest {

    @Test
    void usesConversationRequestedSlotAsFallbackFocusWhenModelDoesNotPickOne() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = new VisionPromptUnderstandingService(
                agentProperties,
                new LocalAdminAgentPromptTranslator(),
                new VisionSemanticMapper(),
                new PromptSemanticsSupport()
        );
        AppUser user = new AppUser();
        user.setId(7L);
        var conversation = VisionConversationTestBuilder.createQuest(1L, user)
                .requestedSlot("location_mode")
                .build();

        VisionPromptUnderstandingResult result = service.understandPrompt("use profile location", conversation);

        assertEquals("location_mode", result.getFocusSlotId());
        assertEquals(0.85d, result.getFocusSlotConfidence());
    }

    @Test
    void buildsCreateQuestSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = new VisionPromptUnderstandingService(
                agentProperties,
                new LocalAdminAgentPromptTranslator(),
                new VisionSemanticMapper(),
                new PromptSemanticsSupport()
        );

        VisionPromptUnderstandingResult result = service.understandPrompt("I need someone to move a sofa", null);

        assertEquals("CREATE_QUEST", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("create_quest", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals(0.8d, result.semanticPlanOrEmpty().getCandidateIntentConfidence());
    }

    @Test
    void buildsDiscoverySemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = new VisionPromptUnderstandingService(
                agentProperties,
                new LocalAdminAgentPromptTranslator(),
                new VisionSemanticMapper(),
                new PromptSemanticsSupport()
        );

        VisionPromptUnderstandingResult result = service.understandPrompt("show me open quests for moving help", null);

        assertEquals("DISCOVER_QUESTS", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("discover_quests", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("moving help", result.semanticPlanOrEmpty().searchQueryOrEmpty());
    }

    @Test
    void defaultsSemanticPlanToUnsupportedWhenPromptHasNoSupportedIntent() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = new VisionPromptUnderstandingService(
                agentProperties,
                new LocalAdminAgentPromptTranslator(),
                new VisionSemanticMapper(),
                new PromptSemanticsSupport()
        );

        VisionPromptUnderstandingResult result = service.understandPrompt("show me the weather", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }
}
