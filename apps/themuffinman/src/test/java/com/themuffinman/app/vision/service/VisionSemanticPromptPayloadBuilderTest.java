package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionSemanticPromptPayloadBuilderTest {

    @Test
    void buildsPromptPayloadWithCompactRequestContext() {
        VisionSemanticPromptPayloadBuilder builder = new VisionSemanticPromptPayloadBuilder(
                JsonMapper.builder().findAndAddModules().build(),
                new SemanticAliasRegistry()
        );

        VisionSemanticOrchestrationRequest request = VisionSemanticOrchestrationRequest.builder()
                .contractVersion("vision-semantic-orchestration-v1")
                .rawPrompt("show application #42")
                .userContext(VisionSemanticUserContext.builder()
                        .preferredLocale("en")
                        .timezone("Europe/Zurich")
                        .countryCode("CH")
                        .locality("Zurich")
                        .build())
                .conversationContext(VisionSemanticConversationContext.builder()
                        .conversationId(12L)
                        .requestedSlot("target_application_query")
                        .slotData(Map.of("target_application_query", "#42"))
                        .build())
                .memoryContext(VisionSemanticMemoryContext.builder()
                        .sessionMemory(VisionSemanticSessionMemoryContext.builder()
                                .conversationId(12L)
                                .currentIntent("VIEW_APPLICATION_DETAIL")
                                .requestedSlot("target_application_query")
                                .slotData(Map.of("target_application_query", "#42"))
                                .build())
                        .recentConversations(List.of())
                        .build())
                .runtimeContext(VisionSemanticRuntimeContext.builder()
                        .inputType("text")
                        .clientLocale("en")
                        .clientTimezone("Europe/Zurich")
                        .build())
                .allowedRoutes(List.of(VisionSemanticRouteDescriptor.builder()
                        .routeKey("view_application_detail")
                        .intent("VIEW_APPLICATION_DETAIL")
                        .capabilityId("view_application_detail")
                        .entityFamily("APPLICATION")
                        .purpose("View application details")
                        .mutating(false)
                        .requiresReview(false)
                        .slots(List.of(VisionSemanticSlotDescriptor.builder()
                                .slotId("target_application_query")
                                .fieldName("application_id")
                                .kind("text")
                                .required(true)
                                .description("Application query")
                                .build()))
                        .build()))
                .responseContract(Map.of("format", "VisionPromptUnderstandingResult"))
                .candidateContexts(List.of(VisionCandidateContext.builder()
                        .contractVersion("vision-candidate-context-v1")
                        .family("WORK")
                        .scope("viewer_owned")
                        .requestId("req-1")
                        .complete(true)
                        .totalCandidates(1)
                        .returnedCandidates(1)
                        .retrievalStrategy("authorized_provider")
                        .items(List.of(VisionCandidateItem.builder()
                                .stableCandidateId("quest-42")
                                .family("WORK")
                                .titleOrLabel("Grill help")
                                .build()))
                        .build()))
                .build();

        String input = builder.buildInput(request);

        assertTrue(input.contains("semanticHints"));
        assertTrue(input.contains("familyAliases"));
        assertTrue(input.contains("business"));
        assertTrue(input.contains("show application #42"));
        assertTrue(input.contains("target_application_query"));
        assertTrue(input.contains("Europe/Zurich"));
        assertTrue(input.contains("candidateContexts"));
        assertTrue(input.contains("quest-42"));
    }
}
