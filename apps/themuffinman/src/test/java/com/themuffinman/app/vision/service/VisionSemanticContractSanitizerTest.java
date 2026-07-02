package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionSemanticContractSanitizerTest {

    private final VisionSemanticContractSanitizer sanitizer = new VisionSemanticContractSanitizer();
    private final VisionSemanticRouteCatalogService routeCatalogService = new VisionSemanticRouteCatalogService();

    @Test
    void downgradesUnsupportedCapabilityToUnsupportedAndClearsPayload() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("CREATE_QUEST")
                        .candidateIntentConfidence(0.95d)
                        .capabilityId("delete_everything")
                        .planningNote("Invented capability")
                        .build())
                .focusSlotId("quest_title")
                .focusSlotConfidence(1.0d)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .questTitle("Move my sofa")
                        .questTitleConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("UNSUPPORTED", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", understanding.semanticPlanOrEmpty().getCapabilityId());
        assertNull(understanding.getFocusSlotId());
        assertTrue(understanding.toExtractedSlotMap().isEmpty());
    }

    @Test
    void clearsDisallowedFocusSlotForDiscoveryRoute() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("DISCOVER_QUESTS")
                        .candidateIntentConfidence(0.8d)
                        .capabilityId("discover_quests")
                        .searchQuery("moving help")
                        .build())
                .focusSlotId("reward_amount")
                .focusSlotConfidence(1.0d)
                .slots(new VisionPromptUnderstandingSlots())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("DISCOVER_QUESTS", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("discover_quests", understanding.semanticPlanOrEmpty().getCapabilityId());
        assertEquals("moving help", understanding.semanticPlanOrEmpty().searchQueryOrEmpty());
        assertNull(understanding.getFocusSlotId());
    }

    @Test
    void downgradesIntentCapabilityMismatchToUnsupported() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("DISCOVER_QUESTS")
                        .candidateIntentConfidence(0.81d)
                        .capabilityId("open_chat")
                        .searchQuery("moving")
                        .targetUserQuery("Josip")
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("UNSUPPORTED", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("unsupported", understanding.semanticPlanOrEmpty().getCapabilityId());
        assertNull(understanding.semanticPlanOrEmpty().getSearchQuery());
        assertNull(understanding.semanticPlanOrEmpty().getTargetUserQuery());
    }

    @Test
    void dropsCreateQuestEnumValuesThatViolateRouteSchema() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("CREATE_QUEST")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("create_quest")
                        .build())
                .slots(VisionPromptUnderstandingSlots.builder()
                        .questTitle("Move my sofa")
                        .questTitleConfidence(1.0d)
                        .visibility("PRIVATE")
                        .visibilityConfidence(1.0d)
                        .reward(VisionPromptUnderstandingRewardSlots.builder()
                                .amount("20")
                                .amountConfidence(1.0d)
                                .build())
                        .schedule(VisionPromptUnderstandingScheduleSlots.builder()
                                .mode("whenever")
                                .modeConfidence(1.0d)
                                .scheduledDate("2026-07-08")
                                .scheduledDateConfidence(1.0d)
                                .build())
                        .location(VisionPromptUnderstandingLocationSlots.builder()
                                .mode("earth")
                                .modeConfidence(1.0d)
                                .label("Main station")
                                .labelConfidence(1.0d)
                                .candidateConfirmation("confirmed")
                                .candidateConfirmationConfidence(1.0d)
                                .build())
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("Move my sofa", understanding.toExtractedSlotMap().get("quest_title"));
        assertEquals("20", understanding.toExtractedSlotMap().get("reward_amount"));
        assertEquals("2026-07-08", understanding.toExtractedSlotMap().get("scheduled_date"));
        assertEquals("Main station", understanding.toExtractedSlotMap().get("location_label"));
        assertNull(understanding.toExtractedSlotMap().get("visibility"));
        assertNull(understanding.toExtractedSlotMap().get("schedule_mode"));
        assertNull(understanding.toExtractedSlotMap().get("location_mode"));
        assertNull(understanding.toExtractedSlotMap().get("location_candidate_confirmation"));
    }

    @Test
    void keepsOnlySelectedRouteGenericPlanFields() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("OPEN_CHAT")
                        .candidateIntentConfidence(0.88d)
                        .capabilityId("open_chat")
                        .searchQuery("should be removed")
                        .targetUserQuery("Josip")
                        .build())
                .slots(VisionPromptUnderstandingSlots.builder()
                        .questDescription("Should not survive")
                        .questDescriptionConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("OPEN_CHAT", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("open_chat", understanding.semanticPlanOrEmpty().getCapabilityId());
        assertNull(understanding.semanticPlanOrEmpty().getSearchQuery());
        assertEquals("Josip", understanding.semanticPlanOrEmpty().getTargetUserQuery());
        assertTrue(understanding.toExtractedSlotMap().isEmpty());
    }

    @Test
    void keepsProfileSlotsOnlyForUpdateProfileRoute() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("UPDATE_PROFILE")
                        .candidateIntentConfidence(0.91d)
                        .capabilityId("update_profile")
                        .build())
                .focusSlotId("profile_description")
                .focusSlotConfidence(1.0d)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .profileUsername("jsak")
                        .profileUsernameConfidence(1.0d)
                        .profileDescription("Reliable mover")
                        .profileDescriptionConfidence(1.0d)
                        .questTitle("Should not survive")
                        .questTitleConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("UPDATE_PROFILE", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("jsak", understanding.toExtractedSlotMap().get("profile_username"));
        assertEquals("Reliable mover", understanding.toExtractedSlotMap().get("profile_description"));
        assertNull(understanding.toExtractedSlotMap().get("quest_title"));
        assertEquals("profile_description", understanding.getFocusSlotId());
    }

    @Test
    void keepsApplicationSlotsOnlyForCreateApplicationRoute() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("CREATE_APPLICATION")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("create_application")
                        .build())
                .focusSlotId("application_message")
                .focusSlotConfidence(1.0d)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .applicationQuestQuery("Move a sofa")
                        .applicationQuestQueryConfidence(1.0d)
                        .applicationMessage("I can help tomorrow")
                        .applicationMessageConfidence(1.0d)
                        .applicationProposedPrice("20")
                        .applicationProposedPriceConfidence(1.0d)
                        .profileUsername("should not survive")
                        .profileUsernameConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("CREATE_APPLICATION", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("Move a sofa", understanding.toExtractedSlotMap().get("target_quest_query"));
        assertEquals("I can help tomorrow", understanding.toExtractedSlotMap().get("application_message"));
        assertEquals("20", understanding.toExtractedSlotMap().get("application_proposed_price"));
        assertNull(understanding.toExtractedSlotMap().get("profile_username"));
        assertEquals("application_message", understanding.getFocusSlotId());
    }

    @Test
    void keepsApplicationSlotsForUpdateApplicationRoute() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("UPDATE_APPLICATION")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("update_application")
                        .build())
                .focusSlotId("application_proposed_price")
                .focusSlotConfidence(1.0d)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .applicationQuestQuery("Move a sofa")
                        .applicationQuestQueryConfidence(1.0d)
                        .applicationMessage("Use my updated message")
                        .applicationMessageConfidence(1.0d)
                        .applicationProposedPrice("25")
                        .applicationProposedPriceConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("UPDATE_APPLICATION", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("Move a sofa", understanding.toExtractedSlotMap().get("target_quest_query"));
        assertEquals("Use my updated message", understanding.toExtractedSlotMap().get("application_message"));
        assertEquals("25", understanding.toExtractedSlotMap().get("application_proposed_price"));
        assertEquals("application_proposed_price", understanding.getFocusSlotId());
    }

    @Test
    void keepsOnlyQuestTargetForWithdrawApplicationRoute() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("WITHDRAW_APPLICATION")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("withdraw_application")
                        .build())
                .focusSlotId("application_message")
                .focusSlotConfidence(1.0d)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .applicationQuestQuery("Move a sofa")
                        .applicationQuestQueryConfidence(1.0d)
                        .applicationMessage("Should not survive")
                        .applicationMessageConfidence(1.0d)
                        .applicationProposedPrice("25")
                        .applicationProposedPriceConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("WITHDRAW_APPLICATION", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("Move a sofa", understanding.toExtractedSlotMap().get("target_quest_query"));
        assertNull(understanding.toExtractedSlotMap().get("application_message"));
        assertNull(understanding.toExtractedSlotMap().get("application_proposed_price"));
        assertNull(understanding.getFocusSlotId());
    }

    @Test
    void keepsCircleSlotsOnlyForUpdateCircleRoute() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("UPDATE_CIRCLE")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("update_circle")
                        .build())
                .focusSlotId("circle_name")
                .focusSlotConfidence(1.0d)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .targetCircleQuery("Neighbours")
                        .targetCircleQueryConfidence(1.0d)
                        .circleName("Core Team")
                        .circleNameConfidence(1.0d)
                        .profileUsername("should not survive")
                        .profileUsernameConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("UPDATE_CIRCLE", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("Neighbours", understanding.toExtractedSlotMap().get("target_circle_query"));
        assertEquals("Core Team", understanding.toExtractedSlotMap().get("circle_name"));
        assertNull(understanding.toExtractedSlotMap().get("profile_username"));
        assertEquals("circle_name", understanding.getFocusSlotId());
    }

    @Test
    void keepsQuestAndApplicantOnlyForApproveApplicationRoute() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("APPROVE_APPLICATION")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("approve_application")
                        .targetUserQuery("Josip")
                        .build())
                .focusSlotId("target_user")
                .focusSlotConfidence(1.0d)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .applicationQuestQuery("Move a sofa")
                        .applicationQuestQueryConfidence(1.0d)
                        .applicationMessage("should not survive")
                        .applicationMessageConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("APPROVE_APPLICATION", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("Josip", understanding.semanticPlanOrEmpty().getTargetUserQuery());
        assertEquals("Move a sofa", understanding.toExtractedSlotMap().get("target_quest_query"));
        assertNull(understanding.toExtractedSlotMap().get("application_message"));
        assertEquals("target_user", understanding.getFocusSlotId());
    }

    @Test
    void keepsProfileLocationSlotsOnlyForProfileLocationRoute() {
        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .semanticPlan(VisionSemanticPlan.builder()
                        .candidateIntent("UPDATE_PROFILE_LOCATION")
                        .candidateIntentConfidence(0.9d)
                        .capabilityId("update_profile_location")
                        .build())
                .focusSlotId("profile_location_label")
                .focusSlotConfidence(1.0d)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .profileLocationMode("EXACT")
                        .profileLocationModeConfidence(1.0d)
                        .profileLocationLabel("Zurich, Switzerland")
                        .profileLocationLabelConfidence(1.0d)
                        .questTitle("should not survive")
                        .questTitleConfidence(1.0d)
                        .build())
                .build();

        sanitizer.sanitize(understanding, allowedRoutes());

        assertEquals("UPDATE_PROFILE_LOCATION", understanding.semanticPlanOrEmpty().getCandidateIntent());
        assertEquals("EXACT", understanding.toExtractedSlotMap().get("profile_location_mode"));
        assertEquals("Zurich, Switzerland", understanding.toExtractedSlotMap().get("profile_location_label"));
        assertNull(understanding.toExtractedSlotMap().get("quest_title"));
        assertEquals("profile_location_label", understanding.getFocusSlotId());
    }

    private List<VisionSemanticRouteDescriptor> allowedRoutes() {
        AppUser user = new AppUser();
        user.setId(7L);
        return routeCatalogService.allowedRoutes(user);
    }
}
