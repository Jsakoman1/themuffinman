package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.semantic.SemanticEntityResolution;
import com.themuffinman.app.semantic.SemanticEntityResolutionStatus;
import com.themuffinman.app.semantic.VisionEntityResolver;
import com.themuffinman.app.semantic.VisionEntityResolverRegistry;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void localEmergencyFallbackKeepsRequestedSlotWhenItCannotSafelyClassifyATopicSwitch() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);
        AppUser user = new AppUser();
        user.setId(7L);
        var conversation = VisionConversationTestBuilder.createQuest(1L, user)
                .requestedSlot("reward_amount")
                .build();

        VisionPromptUnderstandingResult result = service.understandPrompt("create circle Neighbours", conversation);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("reward_amount", result.getFocusSlotId());
        assertEquals(0.85d, result.getFocusSlotConfidence());
    }

    @Test
    void buildsCreateQuestSemanticPlanWithLocalFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("I need someone to move a sofa", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_PROVIDER_LOCAL, result.getUnderstandingProvider());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_STATUS_LOCAL_FAIL_CLOSED, result.getUnderstandingStatus());
        assertEquals("I need someone to move a sofa", result.semanticEnvelopeOrEmpty().getRawUserText());
        assertEquals(SemanticEntityFamily.UNKNOWN, result.semanticEnvelopeOrEmpty().getEntityFamily());
    }

    @Test
    void buildsCreateQuestSemanticPlanWithFurnitureMovingFallbackSignals() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt(
                "i need somebody to help me mova my sofa in my apartment. maybe on weekend, saturday, in the evening, around 8 oclock ? i can pay 20 dollars.",
                null
        );

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_STATUS_LOCAL_FAIL_CLOSED, result.getUnderstandingStatus());
    }

    @Test
    void failsClosedForNonEnglishPromptWhenOpenAiIsUnavailable() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt(
                "napravi mi quest za idući utorak u 5 popodne da mi netko opere kofere",
                null
        );

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("unknown", result.getSourceLanguage());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_STATUS_LOCAL_FAIL_CLOSED, result.getUnderstandingStatus());
    }

    @Test
    void failClosesCreateCirclePromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("create circle Neighbours", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesCreateCircleRequestPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("send circle request to Josip", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesAcceptCircleRequestPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("accept circle request from Josip", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesDeleteCircleRequestPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("decline circle request from Josip", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesUpdateCirclePromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("rename circle Neighbours to Core Team", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesDeleteCirclePromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("delete circle Neighbours", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesCreateApplicationPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("apply to quest 42", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesUpdateApplicationPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("update my application for quest 42", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesWithdrawApplicationPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("withdraw my application for quest 42", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesApproveApplicationPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("approve application Josip for quest 42", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesDeclineApplicationPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("decline application Josip for quest 42", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesUpdateProfilePromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("update my profile", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
    }

    @Test
    void failClosesUpdateProfileLocationPromptWithLocalEmergencyFallback() {
        AgentProperties agentProperties = new AgentProperties();
        VisionPromptUnderstandingService service = service(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("set my location to Zurich, Switzerland", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", result.semanticPlanOrEmpty().getCapabilityId());
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
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_STATUS_LOCAL_EMERGENCY, result.getUnderstandingStatus());
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
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_STATUS_LOCAL_FAIL_CLOSED, result.getUnderstandingStatus());
    }

    @Test
    void usesOpenAiPrimaryResultWhenConfigured() {
        AgentProperties agentProperties = configuredOpenAiProperties();
        VisionPromptUnderstandingService service = service(agentProperties, """
                {
                  "sourceLanguage":"hr",
                  "normalizedPrompt":"create a quest for next Tuesday at 5 pm to wash my suitcases",
                  "semanticPlan":{
                    "candidateIntent":"CREATE_QUEST",
                    "candidateIntentConfidence":0.96,
                    "capabilityId":"create_quest",
                    "planningNote":"Model selected create quest."
                  },
                  "translationApplied":true,
                  "translationReliable":true
                }
                """);

        AppUser user = new AppUser();
        user.setId(7L);

        VisionPromptUnderstandingResult result = service.understandPrompt("napravi mi quest za idući utorak u 5 popodne", null, user);

        assertEquals("CREATE_QUEST", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_PROVIDER_OPENAI, result.getUnderstandingProvider());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_STATUS_OPENAI_PRIMARY, result.getUnderstandingStatus());
        assertEquals("openai", result.getTranslationProvider());
    }

    @Test
    void canonicalizesOpenAiTargetQueriesThroughSemanticAliases() {
        AgentProperties agentProperties = configuredOpenAiProperties();
        VisionPromptUnderstandingService service = service(
                agentProperties,
                buildSemanticPayloadWithQuestAlias(
                        "CREATE_APPLICATION",
                        "create_application",
                        "apply to wash my suitcases",
                        "wash my suitcases"
                )
        );

        AppUser user = new AppUser();
        user.setId(7L);

        VisionPromptUnderstandingResult result = service.understandPrompt("create quest to wash my suitcases", null, user);

        assertEquals("CREATE_APPLICATION", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("wash my luggage", result.semanticEnvelopeOrEmpty().getTargetEntityQuery());
        assertEquals("wash my luggage", result.semanticEnvelopeOrEmpty().slotCandidatesOrEmpty().get("target_quest_query"));
    }

    @Test
    void rescuesSafeReadOnlyPromptWhenOpenAiStaysUnsupported() {
        AgentProperties agentProperties = configuredOpenAiProperties();
        VisionPromptUnderstandingService service = service(agentProperties, """
                {
                  "sourceLanguage":"en",
                  "normalizedPrompt":"show my profile",
                  "semanticPlan":{
                    "candidateIntent":"UNSUPPORTED",
                    "candidateIntentConfidence":0.2,
                    "capabilityId":"unsupported",
                    "planningNote":"Model stayed uncertain."
                  },
                  "translationApplied":false,
                  "translationReliable":true
                }
                """);

        AppUser user = new AppUser();
        user.setId(7L);

        VisionPromptUnderstandingResult result = service.understandPrompt("show my profile", null, user);

        assertEquals("VIEW_PROFILE", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_STATUS_OPENAI_LOCAL_RESCUE, result.getUnderstandingStatus());
    }

    @Test
    void failClosesMutationPromptWhenOpenAiCallFails() {
        AgentProperties agentProperties = configuredOpenAiProperties();
        VisionPromptUnderstandingService service = failingOpenAiService(agentProperties);

        VisionPromptUnderstandingResult result = service.understandPrompt("create quest to move my sofa", null);

        assertEquals("UNSUPPORTED", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_PROVIDER_LOCAL, result.getUnderstandingProvider());
        assertEquals(VisionPromptUnderstandingService.UNDERSTANDING_STATUS_LOCAL_FAIL_CLOSED, result.getUnderstandingStatus());
    }

    @Test
    void flagsLowConfidenceEntityResolutionForClarificationWhenMutatingTargetNeedsDisambiguation() {
        AgentProperties agentProperties = configuredOpenAiProperties();
        VisionPromptUnderstandingService service = service(
                agentProperties,
                buildSemanticPayload(
                        "CREATE_CIRCLE_REQUEST",
                        "create_circle_request",
                        "send a circle request to friend",
                        "friend"
                ),
                new VisionEntityResolverRegistry(List.of(new VisionEntityResolver<VisionResolvedUserTarget>() {
                    @Override
                    public SemanticEntityFamily family() {
                        return SemanticEntityFamily.USER;
                    }

                    @Override
                    public SemanticEntityResolution<VisionResolvedUserTarget> resolve(AppUser currentUser, String targetEntityQuery) {
                        return SemanticEntityResolution.<VisionResolvedUserTarget>builder()
                                .entityFamily(SemanticEntityFamily.USER)
                                .status(SemanticEntityResolutionStatus.RESOLVED)
                                .targetEntityQuery(targetEntityQuery)
                                .entity(new VisionResolvedUserTarget(7L, "Josip", null))
                                .canonicalLabel("Josip")
                                .confidence(0.82d)
                                .build();
                    }
                }), new SemanticAliasRegistry())
        );

        AppUser user = new AppUser();
        user.setId(7L);
        VisionPromptUnderstandingResult result = service.understandPrompt("send a circle request to friend", null, user);

        assertEquals("CREATE_CIRCLE_REQUEST", result.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals(SemanticEntityFamily.CIRCLE, result.semanticEnvelopeOrEmpty().getEntityFamily());
        assertEquals(0.82d, result.semanticEnvelopeOrEmpty().getEntityResolutionConfidence());
        assertEquals(SemanticEntityResolutionStatus.RESOLVED, result.semanticEnvelopeOrEmpty().getEntityResolutionStatus());
        assertEquals("Josip", result.semanticEnvelopeOrEmpty().getEntityResolutionLabel());
        assertTrue(result.semanticEnvelopeOrEmpty().isClarificationRequired());
    }

    private VisionPromptUnderstandingService service(AgentProperties agentProperties) {
        return service(agentProperties, null, new VisionEntityResolverRegistry(List.of(), new SemanticAliasRegistry()));
    }

    private VisionPromptUnderstandingService service(AgentProperties agentProperties, String openAiOutputText) {
        return service(agentProperties, openAiOutputText, new VisionEntityResolverRegistry(List.of(), new SemanticAliasRegistry()));
    }

    private VisionPromptUnderstandingService service(
            AgentProperties agentProperties,
            String openAiOutputText,
            VisionEntityResolverRegistry visionEntityResolverRegistry
    ) {
        return new VisionPromptUnderstandingService(
                agentProperties,
                new VisionSemanticMapper(),
                new PromptSemanticsSupport(),
                new VisionSemanticOrchestrationContextService(new VoiceProperties()),
                new VisionSemanticRouteCatalogService(),
                new VisionSemanticContractSanitizer(),
                new VisionSemanticResponseValidator(),
                visionEntityResolverRegistry,
                new SemanticAliasRegistry()
        ) {
            @Override
            protected String requestOpenAiOutputText(Map<String, Object> payload) {
                if (openAiOutputText == null) {
                    throw new org.springframework.web.client.RestClientException("openai disabled in test");
                }
                return openAiOutputText;
            }
        };
    }

    private VisionPromptUnderstandingService failingOpenAiService(AgentProperties agentProperties) {
        return new VisionPromptUnderstandingService(
                agentProperties,
                new VisionSemanticMapper(),
                new PromptSemanticsSupport(),
                new VisionSemanticOrchestrationContextService(new VoiceProperties()),
                new VisionSemanticRouteCatalogService(),
                new VisionSemanticContractSanitizer(),
                new VisionSemanticResponseValidator(),
                new VisionEntityResolverRegistry(List.of(), new SemanticAliasRegistry()),
                new SemanticAliasRegistry()
        ) {
            @Override
            protected String requestOpenAiOutputText(Map<String, Object> payload) {
                throw new org.springframework.web.client.RestClientException("boom");
            }
        };
    }

    private String buildSemanticPayload(String candidateIntent, String capabilityId, String normalizedPrompt, String targetUserQuery) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(Map.of(
                    "sourceLanguage", "en",
                    "normalizedPrompt", normalizedPrompt,
                    "semanticPlan", Map.of(
                            "candidateIntent", candidateIntent,
                            "candidateIntentConfidence", 0.93d,
                            "capabilityId", capabilityId,
                            "planningNote", "test payload",
                            "targetUserQuery", targetUserQuery
                    ),
                    "translationApplied", false,
                    "translationReliable", true
            ));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String buildSemanticPayloadWithQuestAlias(String candidateIntent, String capabilityId, String normalizedPrompt, String targetQuestQuery) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(Map.of(
                    "sourceLanguage", "en",
                    "normalizedPrompt", normalizedPrompt,
                    "semanticPlan", Map.of(
                            "candidateIntent", candidateIntent,
                            "candidateIntentConfidence", 0.93d,
                            "capabilityId", capabilityId,
                            "planningNote", "test payload"
                    ),
                    "slots", Map.of(
                            "applicationQuestQuery", targetQuestQuery,
                            "applicationQuestQueryConfidence", 0.9d
                    ),
                    "translationApplied", false,
                    "translationReliable", true
            ));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private AgentProperties configuredOpenAiProperties() {
        AgentProperties agentProperties = new AgentProperties();
        agentProperties.setProvider("openai");
        agentProperties.setApiKey("test-key");
        agentProperties.setBaseUrl("https://example.test/v1");
        return agentProperties;
    }
}
