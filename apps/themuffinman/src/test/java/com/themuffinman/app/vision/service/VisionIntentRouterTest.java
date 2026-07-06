package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.model.VisionIntent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionIntentRouterTest {

    @Test
    void usesSemanticPlanBeforePromptHeuristics() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("please help")
                .semanticPlan(VisionSemanticPlan.createQuest(0.95d, "model classified create quest"))
                .build();

        assertEquals(VisionIntent.CREATE_QUEST, router.detectIntent("please help", understanding));
    }

    @Test
    void fallsBackToPromptHeuristicsWhenSemanticPlanIsUnsupported() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("I need help moving")
                .semanticPlan(VisionSemanticPlan.empty())
                .build();

        assertEquals(VisionIntent.CREATE_QUEST, router.detectIntent("I need help moving", understanding));
    }

    @Test
    void routesDiscoveryIntentFromSemanticPlan() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("show me open quests")
                .semanticPlan(VisionSemanticPlan.discoverQuests(0.95d, "browse available quests", "moving help"))
                .build();

        assertEquals(VisionIntent.DISCOVER_QUESTS, router.detectIntent("show me open quests", understanding));
    }

    @Test
    void routesOpenChatIntentFromSemanticPlan() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
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

    @Test
    void routesSettingsAndDetailReadOnlyIntentsFromPromptHeuristics() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());

        assertEquals(VisionIntent.VIEW_CHAT_WORKSPACE, router.detectIntent("show chat"));
        assertEquals(VisionIntent.VIEW_CIRCLES, router.detectIntent("show circles"));
        assertEquals(VisionIntent.VIEW_APPLICATIONS, router.detectIntent("show applications"));
        assertEquals(VisionIntent.VIEW_SETTINGS, router.detectIntent("show settings"));
        assertEquals(VisionIntent.VIEW_USER_PROFILE, router.detectIntent("show user Josip"));
        assertEquals(VisionIntent.VIEW_CIRCLE_DETAIL, router.detectIntent("open circle Family"));
        assertEquals(VisionIntent.VIEW_QUEST_DETAIL, router.detectIntent("show quest #42"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("show notifications"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("view inbox"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("show inbox"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("my notifications"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("open alerts"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("open notification center"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("notification center"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("notification hub"));
        assertEquals(VisionIntent.VIEW_NOTIFICATIONS, router.detectIntent("alerts inbox"));
        assertEquals(VisionIntent.VIEW_QUEST_NEWS, router.detectIntent("show my news"));
        assertEquals(VisionIntent.VIEW_APPLICATION_DETAIL, router.detectIntent("show application #42"));
        assertEquals(VisionIntent.VIEW_THINGS, router.detectIntent("show available listings"));
    }

    @Test
    void prefersCreateCircleOverCirclesSnapshotWhenPromptIsExplicit() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("make new circle of friends")
                .semanticPlan(VisionSemanticPlan.viewCircles(0.94d, "model drifted to the circles snapshot"))
                .build();

        assertEquals(VisionIntent.CREATE_CIRCLE, router.detectIntent("make new circle of friends", understanding));
    }

    @Test
    void prefersCircleRequestOverCirclesSnapshotWhenPromptIsExplicit() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("invite Josip to my circle")
                .semanticPlan(VisionSemanticPlan.viewCircles(0.94d, "model drifted to the circles snapshot"))
                .build();

        assertEquals(VisionIntent.CREATE_CIRCLE_REQUEST, router.detectIntent("invite Josip to my circle", understanding));
    }

    @Test
    void prefersApplicationMutationOverApplicationsSnapshotWhenPromptIsExplicit() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("apply to quest 42")
                .semanticPlan(VisionSemanticPlan.viewApplications(0.94d, "model drifted to applications snapshot"))
                .build();

        assertEquals(VisionIntent.CREATE_APPLICATION, router.detectIntent("apply to quest 42", understanding));
    }

    @Test
    void prefersProfileUpdateOverProfileSnapshotWhenPromptIsExplicit() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("update my profile bio")
                .semanticPlan(VisionSemanticPlan.viewProfile(0.94d, "model drifted to profile snapshot"))
                .build();

        assertEquals(VisionIntent.UPDATE_PROFILE, router.detectIntent("update my profile bio", understanding));
    }

    @Test
    void prefersOpenChatOverChatWorkspaceSnapshotWhenPromptIsExplicit() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("message Josip")
                .semanticPlan(VisionSemanticPlan.viewChatWorkspace(0.94d, "model drifted to chat workspace"))
                .build();

        assertEquals(VisionIntent.OPEN_CHAT, router.detectIntent("message Josip", understanding));
    }

    @Test
    void prefersCircleUpdateOverCircleDetailSnapshotWhenPromptIsExplicit() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("rename circle Neighbours to Core Team")
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent(VisionIntent.VIEW_CIRCLE_DETAIL.name())
                        .candidateIntentConfidence(0.94d)
                        .capabilityId("view_circle_detail")
                        .planningNote("model drifted to circle detail")
                        .build())
                .build();

        assertEquals(VisionIntent.UPDATE_CIRCLE, router.detectIntent("rename circle Neighbours to Core Team", understanding));
    }

    @Test
    void prefersApplicationCreationOverQuestDetailSnapshotWhenPromptIsExplicit() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("apply to this quest")
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent(VisionIntent.VIEW_QUEST_DETAIL.name())
                        .candidateIntentConfidence(0.94d)
                        .capabilityId("view_quest_detail")
                        .planningNote("model drifted to quest detail")
                        .build())
                .build();

        assertEquals(VisionIntent.CREATE_APPLICATION, router.detectIntent("apply to this quest", understanding));
    }

    @Test
    void prefersProfileLocationUpdateOverSettingsSnapshotWhenPromptIsExplicit() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .normalizedPrompt("turn off my location")
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent(VisionIntent.VIEW_SETTINGS.name())
                        .candidateIntentConfidence(0.94d)
                        .capabilityId("view_settings")
                        .planningNote("model drifted to settings")
                        .build())
                .build();

        assertEquals(VisionIntent.UPDATE_PROFILE_LOCATION, router.detectIntent("turn off my location", understanding));
    }

    @Test
    void routesBroadSearchPromptsAndAvoidsSubstringFalsePositives() {
        VisionProperties visionProperties = new VisionProperties();
        VisionIntentRouter router = new VisionIntentRouter(visionProperties, new VisionSemanticRouteCatalogService());

        assertEquals(VisionIntent.UNSUPPORTED, router.detectIntent("jobless market update"));
        assertEquals(VisionIntent.DISCOVER_QUESTS, router.detectIntent("looking for jobs nearby"));
        assertEquals(VisionIntent.SEARCH, router.detectIntent("find people who can help move sofa"));
    }
}
