package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.service.LocalAdminAgentPromptTranslator;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
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
                new VisionSemanticMapper()
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
}
