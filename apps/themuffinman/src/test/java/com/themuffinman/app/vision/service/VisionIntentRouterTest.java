package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.model.VisionIntent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionIntentRouterTest {

    @Test
    void usesSemanticPlanBeforePromptHeuristics() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties);
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("please help")
                .semanticPlan(VisionSemanticPlan.createQuest(0.95d, "model classified create quest"))
                .build();

        assertEquals(VisionIntent.CREATE_QUEST, router.detectIntent("please help", understanding));
    }

    @Test
    void fallsBackToPromptHeuristicsWhenSemanticPlanIsUnsupported() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties);
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("I need help moving")
                .semanticPlan(VisionSemanticPlan.empty())
                .build();

        assertEquals(VisionIntent.CREATE_QUEST, router.detectIntent("I need help moving", understanding));
    }

    @Test
    void routesDiscoveryIntentFromSemanticPlan() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties);
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("show me open quests")
                .semanticPlan(VisionSemanticPlan.discoverQuests(0.95d, "browse available quests", "moving help"))
                .build();

        assertEquals(VisionIntent.DISCOVER_QUESTS, router.detectIntent("show me open quests", understanding));
    }

    @Test
    void routesOpenChatIntentFromSemanticPlan() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties);
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("chat with Josip")
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent(VisionIntent.OPEN_CHAT.name())
                        .candidateIntentConfidence(0.92d)
                        .capabilityId("open_chat")
                        .planningNote("Open a chat conversation with a contact.")
                        .targetUserQuery("Josip")
                        .build())
                .build();

        assertEquals(VisionIntent.OPEN_CHAT, router.detectIntent("chat with Josip", understanding));
    }
}
