package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisionSemanticResponseValidatorTest {

    private final VisionSemanticResponseValidator validator = new VisionSemanticResponseValidator();
    private final VisionSemanticRouteCatalogService routeCatalogService = new VisionSemanticRouteCatalogService();

    @Test
    void acceptsValidCreateQuestResponseShape() {
        AppUser user = new AppUser();
        user.setId(1L);

        VisionSemanticOrchestrationRequest request = request();
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("CREATE_QUEST")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("create_quest")
                        .planningNote("Create a quest")
                        .build())
                .focusSlotId("quest_title")
                .focusSlotConfidence(1.0d)
                .build();

        assertDoesNotThrow(() -> validator.validate(understanding, request));
    }

    @Test
    void rejectsUnsupportedCapabilityIdBeforeSanitization() {
        VisionSemanticOrchestrationRequest request = request();
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("CREATE_QUEST")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("make_magic")
                        .planningNote("Invented capability")
                        .build())
                .focusSlotId("quest_title")
                .focusSlotConfidence(1.0d)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> validator.validate(understanding, request));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rejectsUnsupportedFocusSlotIdBeforeSanitization() {
        VisionSemanticOrchestrationRequest request = request();
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("CREATE_CIRCLE")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("create_circle")
                        .planningNote("Create a circle")
                        .build())
                .focusSlotId("reward_amount")
                .focusSlotConfidence(1.0d)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> validator.validate(understanding, request));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rejectsUnsupportedExtractedSlotForRouteBeforeSanitization() {
        VisionSemanticOrchestrationRequest request = VisionSemanticOrchestrationRequest.builder()
                .allowedRoutes(routeCatalogService.allowedRoutes(user()))
                .responseContract(Map.of(
                        "candidateIntents", List.of("CREATE_CIRCLE"),
                        "capabilityIds", List.of("create_circle"),
                        "focusSlotIds", List.of("circle_name")
                ))
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("CREATE_CIRCLE")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("create_circle")
                        .planningNote("Create a circle")
                        .build())
                .slots(VisionPromptUnderstandingSlots.builder()
                        .circleName("Neighbours")
                        .circleNameConfidence(1.0d)
                        .questTitle("Should not survive")
                        .questTitleConfidence(1.0d)
                        .build())
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> validator.validate(understanding, request));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rejectsUnsupportedSemanticPlanFieldsBeforeSanitization() {
        VisionSemanticOrchestrationRequest request = VisionSemanticOrchestrationRequest.builder()
                .allowedRoutes(routeCatalogService.allowedRoutes(user()))
                .responseContract(Map.of(
                        "candidateIntents", List.of("CREATE_CIRCLE"),
                        "capabilityIds", List.of("create_circle"),
                        "focusSlotIds", List.of("circle_name")
                ))
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("CREATE_CIRCLE")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("create_circle")
                        .planningNote("Create a circle")
                        .searchQuery("moving help")
                        .targetUserQuery("Josip")
                        .build())
                .slots(VisionPromptUnderstandingSlots.builder()
                        .circleName("Neighbours")
                        .circleNameConfidence(1.0d)
                        .build())
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> validator.validate(understanding, request));

        assertEquals(400, exception.getStatusCode().value());
    }

    private VisionSemanticOrchestrationRequest request() {
        return VisionSemanticOrchestrationRequest.builder()
                .responseContract(Map.of(
                        "candidateIntents", List.of("CREATE_QUEST", "CREATE_CIRCLE"),
                        "capabilityIds", List.of("create_quest", "create_circle"),
                        "focusSlotIds", List.of("quest_title", "circle_name")
                ))
                .build();
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(1L);
        return user;
    }
}
