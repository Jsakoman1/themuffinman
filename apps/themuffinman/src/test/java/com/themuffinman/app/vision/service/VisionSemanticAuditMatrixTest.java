package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticPlan;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.VisionEntityResolverRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionSemanticAuditMatrixTest {

    private final VisionPromptUnderstandingService service = new VisionPromptUnderstandingService(
            configuredOpenAiProperties(),
            new VisionSemanticMapper(),
            new PromptSemanticsSupport(),
            new VisionSemanticOrchestrationContextService(new VoiceProperties()),
            new VisionSemanticRouteCatalogService(),
            new VisionSemanticContractSanitizer(),
            new VisionSemanticResponseValidator(),
            new VisionEntityResolverRegistry(java.util.List.of(), new SemanticAliasRegistry()),
            new SemanticAliasRegistry()
    ) {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        protected String requestOpenAiOutputText(Map<String, Object> payload) {
            String input = String.valueOf(payload.get("input"));
            assertTrue(input.contains("\"semanticHints\""), "OpenAI semantic input must include semantic hints");
            assertTrue(input.contains("\"familyAliases\""), "OpenAI semantic input must include family aliases");
            assertTrue(input.contains("\"examples\""), "OpenAI semantic input must include route examples");
            assertTrue(input.contains("\"aliases\""), "OpenAI semantic input must include slot aliases");
            assertTrue(input.contains("\"antiExamples\""), "OpenAI semantic input must include slot anti-examples");
            assertTrue(input.contains("\"activeSlot\""), "OpenAI semantic input must include active slot context");
            assertTrue(input.contains("\"draftSnapshot\""), "OpenAI semantic input must include draft snapshot context");
            try {
                Matcher matcher = Pattern.compile("\"rawPrompt\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(input);
                String rawPrompt = "";
                if (matcher.find()) {
                    rawPrompt = objectMapper.readTree("\"" + matcher.group(1) + "\"").asText("");
                }
                String lower = rawPrompt.toLowerCase(java.util.Locale.ROOT);
                PromptSemanticPlan plan;
                if (lower.contains("find people who can help move sofa")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("SEARCH")
                            .candidateIntentConfidence(0.91d)
                            .capabilityId("search")
                            .planningNote("Broad cross-entity search.")
                            .searchQuery("move sofa")
                            .build();
                } else if (lower.contains("show available listings")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("VIEW_THINGS")
                            .candidateIntentConfidence(0.89d)
                            .capabilityId("view_things")
                            .planningNote("Read-only things snapshot.")
                            .build();
                } else if (lower.contains("show notifications")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("VIEW_NOTIFICATIONS")
                            .candidateIntentConfidence(0.89d)
                            .capabilityId("view_notifications")
                            .planningNote("Read-only notifications inbox.")
                            .build();
                } else if (lower.contains("show inbox")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("VIEW_NOTIFICATIONS")
                            .candidateIntentConfidence(0.89d)
                            .capabilityId("view_notifications")
                            .planningNote("Read-only notifications inbox.")
                            .build();
                } else if (lower.contains("view inbox")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("VIEW_NOTIFICATIONS")
                            .candidateIntentConfidence(0.89d)
                            .capabilityId("view_notifications")
                            .planningNote("Read-only notifications inbox.")
                            .build();
                } else if (lower.contains("open notification center")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("VIEW_NOTIFICATIONS")
                            .candidateIntentConfidence(0.89d)
                            .capabilityId("view_notifications")
                            .planningNote("Read-only notifications inbox.")
                            .build();
                } else if (lower.contains("notification hub")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("VIEW_NOTIFICATIONS")
                            .candidateIntentConfidence(0.89d)
                            .capabilityId("view_notifications")
                            .planningNote("Read-only notifications inbox.")
                            .build();
                } else if (lower.contains("alerts inbox")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("VIEW_NOTIFICATIONS")
                            .candidateIntentConfidence(0.89d)
                            .capabilityId("view_notifications")
                            .planningNote("Read-only notifications inbox.")
                            .build();
                } else if (lower.contains("my notifications")) {
                    plan = PromptSemanticPlan.builder()
                            .candidateIntent("VIEW_NOTIFICATIONS")
                            .candidateIntentConfidence(0.89d)
                            .capabilityId("view_notifications")
                            .planningNote("Read-only notifications inbox.")
                            .build();
                } else {
                    plan = new PromptSemanticsSupport().inferPlan(rawPrompt);
                }
                Map<String, Object> semanticPlan = new java.util.LinkedHashMap<>();
                semanticPlan.put("candidateIntent", plan.getCandidateIntent());
                semanticPlan.put("candidateIntentConfidence", plan.getCandidateIntentConfidence());
                semanticPlan.put("capabilityId", plan.getCapabilityId());
                semanticPlan.put("planningNote", plan.getPlanningNote());
                semanticPlan.put("searchQuery", plan.getSearchQuery() == null ? "" : plan.getSearchQuery());
                semanticPlan.put("targetUserQuery", plan.getTargetUserQuery() == null ? "" : plan.getTargetUserQuery());
                Map<String, Object> output = new java.util.LinkedHashMap<>();
                output.put("sourceLanguage", "en");
                output.put("normalizedPrompt", rawPrompt);
                output.put("semanticPlan", semanticPlan);
                output.put("translationApplied", false);
                output.put("translationReliable", true);
                return objectMapper.writeValueAsString(Map.of(
                        "sourceLanguage", "en",
                        "normalizedPrompt", rawPrompt,
                        "semanticPlan", semanticPlan,
                        "translationApplied", false,
                        "translationReliable", true
                ));
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }
    };

    @ParameterizedTest(name = "{0}")
    @MethodSource("promptCases")
    void routesCommonPromptShapesIntoTheExpectedSemanticIntent(String label, String prompt, String expectedIntent, String expectedCapabilityId) {
        VisionPromptUnderstandingResult result = service.understandPrompt(prompt, null, testUser());

        assertEquals(expectedIntent, result.semanticPlanOrEmpty().getCandidateIntent(), label);
        assertEquals(expectedCapabilityId, result.semanticPlanOrEmpty().getCapabilityId(), label);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mixedSignalCases")
    void prefersTheMostRelevantEntityFamilyWhenPromptContainsMultipleSignals(String label, String prompt, String expectedIntent, String expectedCapabilityId) {
        VisionPromptUnderstandingResult result = service.understandPrompt(prompt, null, testUser());

        assertEquals(expectedIntent, result.semanticPlanOrEmpty().getCandidateIntent(), label);
        assertEquals(expectedCapabilityId, result.semanticPlanOrEmpty().getCapabilityId(), label);
    }

    @Test
    void recordsClarificationAndReplayMetadataWhenRequiredQuestSlotsAreMissing() {
        VisionPromptUnderstandingResult result = service.understandPrompt(
                "Create a quest",
                null,
                testUser()
        );

        assertTrue(result.semanticEnvelopeOrEmpty().isClarificationRequired());
        assertEquals(
                java.util.List.of("quest_title", "quest_description", "reward_amount", "schedule_mode", "visibility", "location_mode"),
                result.semanticEnvelopeOrEmpty().requiredSlotIdsOrEmpty()
        );
        assertTrue(result.semanticEnvelopeOrEmpty().missingRequiredSlotIdsOrEmpty().contains("quest_title"));
        assertTrue(result.semanticEnvelopeOrEmpty().getReplayRecord() != null);
        assertEquals("vision-semantic-orchestration-v1", result.semanticEnvelopeOrEmpty().getContractVersion());
    }

    @Test
    void emptyUnderstandingStillCarriesReplayMetadataForFallbackPaths() {
        VisionPromptUnderstandingResult result = VisionPromptUnderstandingResult.empty("fallback prompt", "2026-07-04T00:00:00Z");

        assertTrue(result.semanticEnvelopeOrEmpty().getReplayRecord() != null);
        assertEquals("fallback prompt", result.semanticEnvelopeOrEmpty().getReplayRecord().getRawUserText());
        assertEquals("fallback prompt", result.semanticEnvelopeOrEmpty().getReplayRecord().getNormalizedEnglishText());
        assertEquals("UNSUPPORTED", result.semanticEnvelopeOrEmpty().getReplayRecord().getIntent());
        assertEquals("2026-07-04T00:00:00Z", result.semanticEnvelopeOrEmpty().getReplayRecord().getCapturedAt());
    }

    static Stream<Arguments> promptCases() {
        return Stream.of(
                Arguments.of("create quest with split fields", "Create a paid quest called Move my sofa for tomorrow at 7 pm in Zurich for 20 euros", "CREATE_QUEST", "create_quest"),
                Arguments.of("discover moving help", "show me open quests for moving help", "DISCOVER_QUESTS", "discover_quests"),
                Arguments.of("broad search", "find people who can help move sofa", "SEARCH", "search"),
                Arguments.of("open chat with Josip", "open chat with Josip", "OPEN_CHAT", "open_chat"),
                Arguments.of("view chat workspace", "show chat", "VIEW_CHAT_WORKSPACE", "view_chat_workspace"),
                Arguments.of("view profile", "show my profile", "VIEW_PROFILE", "view_profile"),
                Arguments.of("view circles", "show circles", "VIEW_CIRCLES", "view_circles"),
                Arguments.of("view applications", "show applications", "VIEW_APPLICATIONS", "view_applications"),
                Arguments.of("view things", "show available listings", "VIEW_THINGS", "view_things"),
                Arguments.of("view settings", "show settings", "VIEW_SETTINGS", "view_settings"),
                Arguments.of("view chat workspace", "show chat workspace", "VIEW_CHAT_WORKSPACE", "view_chat_workspace"),
                Arguments.of("create circle", "create circle Neighbours", "CREATE_CIRCLE", "create_circle"),
                Arguments.of("create group", "make a group called Neighbours", "CREATE_CIRCLE", "create_circle"),
                Arguments.of("create circle request", "send circle request to Josip", "CREATE_CIRCLE_REQUEST", "create_circle_request"),
                Arguments.of("accept circle request", "accept circle request from Josip", "ACCEPT_CIRCLE_REQUEST", "accept_circle_request"),
                Arguments.of("delete circle request", "decline circle request from Josip", "DELETE_CIRCLE_REQUEST", "delete_circle_request"),
                Arguments.of("update circle", "rename circle Neighbours to Core Team", "UPDATE_CIRCLE", "update_circle"),
                Arguments.of("delete circle", "delete circle Neighbours", "DELETE_CIRCLE", "delete_circle"),
                Arguments.of("create application", "apply to quest 42", "CREATE_APPLICATION", "create_application"),
                Arguments.of("submit application", "submit application for quest 42", "CREATE_APPLICATION", "create_application"),
                Arguments.of("update application", "update my application for quest 42", "UPDATE_APPLICATION", "update_application"),
                Arguments.of("withdraw application", "withdraw my application for quest 42", "WITHDRAW_APPLICATION", "withdraw_application"),
                Arguments.of("approve application", "approve application Josip for quest 42", "APPROVE_APPLICATION", "approve_application"),
                Arguments.of("decline application", "decline application Josip for quest 42", "DECLINE_APPLICATION", "decline_application"),
                Arguments.of("update profile", "update my profile username to jsak and bio to Reliable mover", "UPDATE_PROFILE", "update_profile"),
                Arguments.of("edit profile", "edit profile and change bio", "UPDATE_PROFILE", "update_profile"),
                Arguments.of("update profile location", "set my location to Zurich, Switzerland", "UPDATE_PROFILE_LOCATION", "update_profile_location"),
                Arguments.of("chat message", "send message to Josip", "OPEN_CHAT", "open_chat"),
                Arguments.of("view quest detail", "show quest #42", "VIEW_QUEST_DETAIL", "view_quest_detail"),
                Arguments.of("view quest news", "show my news", "VIEW_QUEST_NEWS", "view_quest_news"),
                Arguments.of("view notifications inbox", "show inbox", "VIEW_NOTIFICATIONS", "view_notifications"),
                Arguments.of("view notifications inbox", "view inbox", "VIEW_NOTIFICATIONS", "view_notifications"),
                Arguments.of("view notifications", "my notifications", "VIEW_NOTIFICATIONS", "view_notifications"),
                Arguments.of("view notification center", "open notification center", "VIEW_NOTIFICATIONS", "view_notifications"),
                Arguments.of("view notification hub", "notification hub", "VIEW_NOTIFICATIONS", "view_notifications"),
                Arguments.of("view alerts inbox", "alerts inbox", "VIEW_NOTIFICATIONS", "view_notifications"),
                Arguments.of("view application detail", "show application #42", "VIEW_APPLICATION_DETAIL", "view_application_detail"),
                Arguments.of("view user profile", "show user Josip", "VIEW_USER_PROFILE", "view_user_profile"),
                Arguments.of("view circle detail", "open circle Family", "VIEW_CIRCLE_DETAIL", "view_circle_detail"),
                Arguments.of("view notifications", "show notifications", "VIEW_NOTIFICATIONS", "view_notifications")
        );
    }

    static Stream<Arguments> mixedSignalCases() {
        return Stream.of(
                Arguments.of("quest beats circles", "create quest still wins when circle words are present", "CREATE_QUEST", "create_quest"),
                Arguments.of("profile beats chat when explicit", "update my profile and also message Josip", "UPDATE_PROFILE", "update_profile")
        );
    }

    private AgentProperties configuredOpenAiProperties() {
        AgentProperties agentProperties = new AgentProperties();
        agentProperties.setProvider("openai");
        agentProperties.setApiKey("test-key");
        agentProperties.setBaseUrl("https://example.test/v1");
        return agentProperties;
    }

    private AppUser testUser() {
        AppUser user = new AppUser();
        user.setId(7L);
        return user;
    }
}
