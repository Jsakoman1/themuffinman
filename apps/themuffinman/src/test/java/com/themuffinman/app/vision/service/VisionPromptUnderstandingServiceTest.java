package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.service.LocalAdminAgentPromptTranslator;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionPromptUnderstandingServiceTest {

    @Test
    void usesConversationRequestedSlotAsFallbackFocusWhenModelDoesNotPickOne() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);
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
    void doesNotReuseConversationRequestedSlotWhenPromptClearlySwitchesEntityFamily() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);
        AppUser user = new AppUser();
        user.setId(7L);
        var conversation = VisionConversationTestBuilder.createQuest(1L, user)
                .requestedSlot("reward_amount")
                .build();

        VisionPromptUnderstandingResult result = service.understandPrompt("create circle Neighbours", conversation);

        assertEquals("CREATE_CIRCLE", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("create_circle", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals(null, result.getFocusSlotId());
        assertEquals(null, result.getFocusSlotConfidence());
    }

    @Test
    void buildsCreateQuestSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("I need someone to move a sofa", null);

        assertEquals("CREATE_QUEST", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("create_quest", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals(0.8d, result.semanticPlanOrEmpty().getCandidateIntentConfidence());
    }

    @Test
    void buildsCreateQuestSemanticPlanWithFurnitureMovingFallbackSignals() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt(
                "i need somebody to help me mova my sofa in my apartment. maybe on weekend, saturday, in the evening, around 8 oclock ? i can pay 20 dollars.",
                null
        );

        assertEquals("CREATE_QUEST", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("create_quest", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsCreateCircleSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("create circle Neighbours", null);

        assertEquals("CREATE_CIRCLE", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("create_circle", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsCreateCircleRequestSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("send circle request to Josip", null);

        assertEquals("CREATE_CIRCLE_REQUEST", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("create_circle_request", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsAcceptCircleRequestSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("accept circle request from Josip", null);

        assertEquals("ACCEPT_CIRCLE_REQUEST", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("accept_circle_request", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsDeleteCircleRequestSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("decline circle request from Josip", null);

        assertEquals("DELETE_CIRCLE_REQUEST", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("delete_circle_request", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsUpdateCircleSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("rename circle Neighbours to Core Team", null);

        assertEquals("UPDATE_CIRCLE", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("update_circle", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsDeleteCircleSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("delete circle Neighbours", null);

        assertEquals("DELETE_CIRCLE", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("delete_circle", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsCreateApplicationSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("apply to quest 42", null);

        assertEquals("CREATE_APPLICATION", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("create_application", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsUpdateApplicationSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("update my application for quest 42", null);

        assertEquals("UPDATE_APPLICATION", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("update_application", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsWithdrawApplicationSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("withdraw my application for quest 42", null);

        assertEquals("WITHDRAW_APPLICATION", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("withdraw_application", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsApproveApplicationSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("approve application Josip for quest 42", null);

        assertEquals("APPROVE_APPLICATION", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("approve_application", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsDeclineApplicationSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("decline application Josip for quest 42", null);

        assertEquals("DECLINE_APPLICATION", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("decline_application", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsUpdateProfileSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("update my profile", null);

        assertEquals("UPDATE_PROFILE", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("update_profile", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsUpdateProfileLocationSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("set my location to Zurich, Switzerland", null);

        assertEquals("UPDATE_PROFILE_LOCATION", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("update_profile_location", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsDiscoverySemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("show me open quests for moving help", null);

        assertEquals("DISCOVER_QUESTS", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("discover_quests", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("moving help", result.semanticPlanOrEmpty().searchQueryOrEmpty());
    }

    @Test
    void buildsProfileSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("show my profile", null);

        assertEquals("VIEW_PROFILE", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_profile", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void buildsSettingsAndDetailSemanticPlansWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult chatWorkspace = service.understandPrompt("show chat", null);
        VisionPromptUnderstandingResult circles = service.understandPrompt("show circles", null);
        VisionPromptUnderstandingResult applications = service.understandPrompt("show applications", null);
        VisionPromptUnderstandingResult settings = service.understandPrompt("show settings", null);
        VisionPromptUnderstandingResult userProfile = service.understandPrompt("show user Josip", null);
        VisionPromptUnderstandingResult circleDetail = service.understandPrompt("open circle Family", null);
        VisionPromptUnderstandingResult questDetail = service.understandPrompt("show quest #42", null);
        VisionPromptUnderstandingResult applicationDetail = service.understandPrompt("show application #42", null);

        assertEquals("VIEW_CHAT_WORKSPACE", chatWorkspace.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_chat_workspace", chatWorkspace.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("VIEW_CIRCLES", circles.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_circles", circles.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("VIEW_APPLICATIONS", applications.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_applications", applications.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("VIEW_SETTINGS", settings.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_settings", settings.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("VIEW_USER_PROFILE", userProfile.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_user_profile", userProfile.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("VIEW_CIRCLE_DETAIL", circleDetail.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_circle_detail", circleDetail.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("VIEW_QUEST_DETAIL", questDetail.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_quest_detail", questDetail.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("VIEW_APPLICATION_DETAIL", applicationDetail.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("view_application_detail", applicationDetail.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void defaultsSemanticPlanToUnsupportedWhenPromptHasNoSupportedIntent() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("show me the weather", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    private VisionPromptUnderstandingService service(AgentProperties agentProperties) {
        return new VisionPromptUnderstandingService(
                agentProperties,
                new LocalAdminAgentPromptTranslator(),
                new VisionSemanticMapper(),
                new PromptSemanticsSupport(),
                new VisionSemanticOrchestrationContextService(new VoiceProperties()),
                new VisionSemanticRouteCatalogService(),
                new VisionSemanticContractSanitizer(),
                new VisionSemanticResponseValidator()
        );
    }
}
