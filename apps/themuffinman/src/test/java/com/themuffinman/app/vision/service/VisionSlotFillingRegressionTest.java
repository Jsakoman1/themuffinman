package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.VisionEntityResolverRegistry;
import com.themuffinman.app.vision.model.VisionIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionSlotFillingRegressionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VisionPromptUnderstandingService service = new VisionPromptUnderstandingService(
            configuredOpenAiProperties(),
            new VisionSemanticMapper(),
            new PromptSemanticsSupport(),
            new VisionSemanticOrchestrationContextService(new VoiceProperties()),
            new VisionSemanticRouteCatalogService(),
            new VisionSemanticContractSanitizer(),
            new VisionSemanticResponseValidator(),
            new VisionEntityResolverRegistry(List.of(), new SemanticAliasRegistry()),
            new SemanticAliasRegistry(),
            new VisionCandidateContextService(List.of())
    ) {
        @Override
        protected String requestOpenAiOutputText(Map<String, Object> payload) {
            String input = String.valueOf(payload.get("input"));
            String rawPrompt = extractRawPrompt(input);
            String lower = rawPrompt.toLowerCase(Locale.ROOT);

            if (lower.contains("create new circle lover")) {
                return buildResponse(
                        "CREATE_CIRCLE",
                        "create_circle",
                        "create new circle Lover",
                        Map.of(
                                "circleName", slotValue("Lover"),
                                "circleNameConfidence", 0.98d
                        ),
                        null,
                        null
                );
            }
            if (lower.contains("show me my circles")) {
                return buildResponse(
                        "VIEW_CIRCLES",
                        "view_circles",
                        "show me my circles",
                        Map.of(),
                        null,
                        null
                );
            }
            if (lower.contains("rename circle friends to core team")) {
                return buildResponse(
                        "UPDATE_CIRCLE",
                        "update_circle",
                        "rename circle Friends to Core Team",
                        Map.of(
                                "targetCircleQuery", slotValue("Friends"),
                                "targetCircleQueryConfidence", 0.97d,
                                "circleName", slotValue("Core Team"),
                                "circleNameConfidence", 0.97d
                        ),
                        null,
                        null
                );
            }
            if (lower.contains("delete circle friends")) {
                return buildResponse(
                        "DELETE_CIRCLE",
                        "delete_circle",
                        "delete circle Friends",
                        Map.of(
                                "targetCircleQuery", slotValue("Friends"),
                                "targetCircleQueryConfidence", 0.97d
                        ),
                        null,
                        null
                );
            }
            if (lower.contains("apply to quest #42")) {
                return buildResponse(
                        "CREATE_APPLICATION",
                        "create_application",
                        "apply to quest #42 i can help tomorrow for 20 euros",
                        Map.of(
                                "applicationQuestQuery", slotValue("#42"),
                                "applicationQuestQueryConfidence", 0.98d,
                                "applicationMessage", slotValue("i can help tomorrow"),
                                "applicationMessageConfidence", 0.96d,
                                "applicationProposedPrice", slotValue("20"),
                                "applicationProposedPriceConfidence", 0.95d
                        ),
                        null,
                        null
                );
            }
            if (lower.contains("show application #42")) {
                return buildResponse(
                        "VIEW_APPLICATION_DETAIL",
                        "view_application_detail",
                        "show application #42",
                        Map.of(
                                "applicationTargetQuery", slotValue("#42"),
                                "applicationTargetQueryConfidence", 0.98d
                        ),
                        null,
                        null
                );
            }
            if (lower.contains("show my applications")) {
                return buildResponse(
                        "VIEW_APPLICATIONS",
                        "view_applications",
                        "show my applications",
                        Map.of(),
                        null,
                        null
                );
            }
            if (lower.contains("update my profile username to jsak and bio to reliable mover")) {
                return buildResponse(
                        "UPDATE_PROFILE",
                        "update_profile",
                        "update my profile username to jsak and bio to reliable mover",
                        Map.of(
                                "profileUsername", slotValue("jsak"),
                                "profileUsernameConfidence", 0.97d,
                                "profileDescription", slotValue("reliable mover"),
                                "profileDescriptionConfidence", 0.96d
                        ),
                        null,
                        null
                );
            }
            if (lower.contains("show my profile")) {
                return buildResponse(
                        "VIEW_PROFILE",
                        "view_profile",
                        "show my profile",
                        Map.of(),
                        null,
                        null
                );
            }
            if (lower.contains("show user josip")) {
                return buildResponse(
                        "VIEW_USER_PROFILE",
                        "view_user_profile",
                        "show user Josip",
                        Map.of(),
                        null,
                        slotValue("Josip")
                );
            }
            if (lower.contains("open chat with josip")) {
                return buildResponse(
                        "OPEN_CHAT",
                        "open_chat",
                        "open chat with Josip",
                        Map.of(),
                        null,
                        slotValue("Josip")
                );
            }
            if (lower.contains("find people who can help move sofa")) {
                return buildResponse(
                        "SEARCH",
                        "search",
                        "find people who can help move sofa",
                        Map.of(),
                        slotValue("move sofa"),
                        null
                );
            }
            if (lower.contains("show me open quests for moving help")) {
                return buildResponse(
                        "DISCOVER_QUESTS",
                        "discover_quests",
                        "show me open quests for moving help",
                        Map.of(),
                        slotValue("moving help"),
                        null
                );
            }
            if (lower.contains("show available listings")) {
                return buildResponse(
                        "VIEW_THINGS",
                        "view_things",
                        "show available listings",
                        Map.of(),
                        null,
                        null
                );
            }
            if (lower.contains("show notifications")) {
                return buildResponse(
                        "VIEW_NOTIFICATIONS",
                        "view_notifications",
                        "show notifications",
                        Map.of(),
                        null,
                        null
                );
            }
            if (lower.contains("show quest #42")) {
                return buildResponse(
                        "VIEW_QUEST_DETAIL",
                        "view_quest_detail",
                        "show quest #42",
                        Map.of(
                                "applicationQuestQuery", slotValue("#42"),
                                "applicationQuestQueryConfidence", 0.98d
                        ),
                        null,
                        null
                );
            }

            throw new IllegalStateException("Unexpected prompt: " + rawPrompt);
        }
    };

    @ParameterizedTest(name = "{0}")
    @MethodSource("utteranceCases")
    void keepsRealCrudUtterancesMappedToStandardVisionContracts(
            String label,
            String prompt,
            String expectedIntent,
            String expectedCapabilityId,
            Map<String, String> expectedSlots,
            String expectedSearchQuery,
            String expectedTargetUserQuery
    ) {
        AppUser user = testUser();
        VisionPromptUnderstandingResult result = service.understandPrompt(prompt, null, user);

        assertEquals(expectedIntent, result.semanticPlanOrEmpty().getCandidateIntent(), label);
        assertEquals(expectedCapabilityId, result.semanticPlanOrEmpty().getCapabilityId(), label);
        if (expectedSearchQuery != null) {
            assertEquals(expectedSearchQuery, result.semanticPlanOrEmpty().searchQueryOrEmpty(), label);
        }
        if (expectedTargetUserQuery != null) {
            assertEquals(expectedTargetUserQuery, result.semanticPlanOrEmpty().getTargetUserQuery(), label);
        }
        expectedSlots.forEach((slotId, expectedValue) ->
                assertEquals(expectedValue, result.toExtractedSlotMap().get(slotId), label + " -> " + slotId));
    }

    @Test
    void keepsCrudRouteShapeStandardizedAcrossEntityFamilies() {
        VisionSemanticRouteCatalogService catalog = new VisionSemanticRouteCatalogService();
        List<VisionSemanticRouteDescriptor> routes = catalog.allRoutes();

        assertStandardRoute(catalog, routes, "vision.create_circle", true, true, "circle");
        assertStandardRoute(catalog, routes, "vision.view_circles", false, false, "circle");
        assertStandardRoute(catalog, routes, "vision.view_circle_detail", false, false, "circle");
        assertStandardRoute(catalog, routes, "vision.update_circle", true, true, "circle");
        assertStandardRoute(catalog, routes, "vision.delete_circle", true, true, "circle");

        assertStandardRoute(catalog, routes, "vision.create_application", true, true, "application");
        assertStandardRoute(catalog, routes, "vision.view_applications", false, false, "application");
        assertStandardRoute(catalog, routes, "vision.view_application_detail", false, false, "application");
        assertStandardRoute(catalog, routes, "vision.update_application", true, true, "application");
        assertStandardRoute(catalog, routes, "vision.withdraw_application", true, true, "application");

        assertStandardRoute(catalog, routes, "vision.view_profile", false, false, "profile");
        assertStandardRoute(catalog, routes, "vision.view_user_profile", false, false, "profile");
        assertStandardRoute(catalog, routes, "vision.update_profile", true, true, "profile");
        assertStandardRoute(catalog, routes, "vision.update_profile_location", true, true, "profile");

        assertStandardRoute(catalog, routes, "vision.create_quest", true, true, "quest");
        assertStandardRoute(catalog, routes, "vision.view_quest_detail", false, false, "quest");
        assertStandardRoute(catalog, routes, "vision.discover_quests", false, false, "quest");
        assertStandardRoute(catalog, routes, "vision.view_quest_news", false, false, "news");
    }

    private static Stream<Arguments> utteranceCases() {
        return Stream.of(
                Arguments.of(
                        "circle create",
                        "create new circle Lover",
                        "CREATE_CIRCLE",
                        "create_circle",
                        Map.of("circle_name", "Lover"),
                        null,
                        null
                ),
                Arguments.of(
                        "circle read snapshot",
                        "show me my circles",
                        "VIEW_CIRCLES",
                        "view_circles",
                        Map.of(),
                        null,
                        null
                ),
                Arguments.of(
                        "circle update",
                        "rename circle Friends to Core Team",
                        "UPDATE_CIRCLE",
                        "update_circle",
                        Map.of(
                                "target_circle_query", "Friends",
                                "circle_name", "Core Team"
                        ),
                        null,
                        null
                ),
                Arguments.of(
                        "circle delete",
                        "delete circle Friends",
                        "DELETE_CIRCLE",
                        "delete_circle",
                        Map.of("target_circle_query", "Friends"),
                        null,
                        null
                ),
                Arguments.of(
                        "application create",
                        "apply to quest #42 i can help tomorrow for 20 euros",
                        "CREATE_APPLICATION",
                        "create_application",
                        Map.of(
                                "target_quest_query", "#42",
                                "application_message", "i can help tomorrow",
                                "application_proposed_price", "20"
                        ),
                        null,
                        null
                ),
                Arguments.of(
                        "application detail",
                        "show application #42",
                        "VIEW_APPLICATION_DETAIL",
                        "view_application_detail",
                        Map.of(),
                        null,
                        null
                ),
                Arguments.of(
                        "profile update",
                        "update my profile username to jsak and bio to reliable mover",
                        "UPDATE_PROFILE",
                        "update_profile",
                        Map.of(
                                "profile_username", "jsak",
                                "profile_description", "reliable mover"
                        ),
                        null,
                        null
                ),
                Arguments.of(
                        "profile read",
                        "show my profile",
                        "VIEW_PROFILE",
                        "view_profile",
                        Map.of(),
                        null,
                        null
                ),
                Arguments.of(
                        "profile target read",
                        "show user Josip",
                        "VIEW_USER_PROFILE",
                        "view_user_profile",
                        Map.of(),
                        null,
                        "Josip"
                ),
                Arguments.of(
                        "chat target",
                        "open chat with Josip",
                        "OPEN_CHAT",
                        "open_chat",
                        Map.of(),
                        null,
                        "Josip"
                ),
                Arguments.of(
                        "quest discovery",
                        "show me open quests for moving help",
                        "DISCOVER_QUESTS",
                        "discover_quests",
                        Map.of(),
                        "moving help",
                        null
                ),
                Arguments.of(
                        "things snapshot",
                        "show available listings",
                        "VIEW_THINGS",
                        "view_things",
                        Map.of(),
                        null,
                        null
                ),
                Arguments.of(
                        "notifications snapshot",
                        "show notifications",
                        "VIEW_NOTIFICATIONS",
                        "view_notifications",
                        Map.of(),
                        null,
                        null
                )
        );
    }

    private void assertStandardRoute(
            VisionSemanticRouteCatalogService catalog,
            List<VisionSemanticRouteDescriptor> routes,
            String routeKey,
            boolean mutating,
            boolean requiresReview,
            String expectedEntityType
    ) {
        VisionSemanticRouteDescriptor route = routes.stream()
                .filter(candidate -> routeKey.equals(candidate.getRouteKey()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing route: " + routeKey));

        assertEquals(expectedEntityType, route.getEntityType(), routeKey);
        assertTrue(route.getIntent() != null && !route.getIntent().isBlank(), routeKey);
        assertTrue(route.getCapabilityId() != null && !route.getCapabilityId().isBlank(), routeKey);
        assertNotNull(route.getPurpose(), routeKey);
        assertEquals(mutating, route.isMutating(), routeKey);
        assertEquals(requiresReview, route.isRequiresReview(), routeKey);

        VisionIntent intent = VisionIntent.valueOf(route.getIntent());
        assertTrue(catalog.dtoTypeForIntent(intent) != null && !catalog.dtoTypeForIntent(intent).isBlank(), routeKey);
        assertTrue(catalog.validatorKeyForIntent(intent) != null && !catalog.validatorKeyForIntent(intent).isBlank(), routeKey);
        assertTrue(catalog.executorKeyForIntent(intent) != null && !catalog.executorKeyForIntent(intent).isBlank(), routeKey);
        assertTrue(catalog.minimumConfidenceForIntent(intent) >= 0.70d, routeKey);

        if (mutating) {
            assertNotNull(route.getExamples(), routeKey);
            assertFalse(route.getExamples().isEmpty(), routeKey);
            assertNotNull(route.getSlots(), routeKey);
            assertFalse(route.getSlots().isEmpty(), routeKey);
            assertTrue(route.getExamples().stream().allMatch(example -> example.getInput() != null && !example.getInput().isBlank()), routeKey);
            assertTrue(route.getSlots().stream().allMatch(slot -> slot.getDescription() != null && !slot.getDescription().isBlank()), routeKey);
            assertTrue(route.getSlots().stream().allMatch(slot -> slot.getAliases() != null), routeKey);
            assertTrue(route.getSlots().stream().allMatch(slot -> slot.getAntiExamples() != null), routeKey);
        }
    }

    private String buildResponse(
            String candidateIntent,
            String capabilityId,
            String normalizedPrompt,
            Map<String, Object> slots,
            String searchQuery,
            String targetUserQuery
    ) {
        try {
            Map<String, Object> semanticPlan = new LinkedHashMap<>();
            semanticPlan.put("candidateIntent", candidateIntent);
            semanticPlan.put("candidateIntentConfidence", 0.96d);
            semanticPlan.put("capabilityId", capabilityId);
            semanticPlan.put("planningNote", "regression payload");
            if (searchQuery != null) {
                semanticPlan.put("searchQuery", searchQuery);
            }
            if (targetUserQuery != null) {
                semanticPlan.put("targetUserQuery", targetUserQuery);
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sourceLanguage", "en");
            payload.put("normalizedPrompt", normalizedPrompt);
            payload.put("semanticPlan", semanticPlan);
            payload.put("translationApplied", false);
            payload.put("translationReliable", true);
            if (slots != null && !slots.isEmpty()) {
                payload.put("slots", slots);
            }

            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String extractRawPrompt(String input) {
        try {
            Matcher matcher = Pattern.compile("\"rawPrompt\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(input);
            if (!matcher.find()) {
                return input;
            }
            return objectMapper.readTree("\"" + matcher.group(1) + "\"").asText("");
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private String slotValue(String value) {
        return value;
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
