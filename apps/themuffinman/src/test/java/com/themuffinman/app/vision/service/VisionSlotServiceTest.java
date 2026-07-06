package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationLookupService;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.testing.VisionConversationTestBuilder;
import com.themuffinman.app.vision.testing.VisionLocationCandidatePresets;
import com.themuffinman.app.testing.TestFixtures;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisionSlotServiceTest {

    private final VisionSlotService visionSlotService = new VisionSlotService(
            new VisionScheduleParserService(),
            new VisionLocationResolutionService(new VisionLocationParserService()),
            new VisionSemanticMapper()
    );

    @Test
    void locationPromptDoesNotLeakIntoDescription() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .requestedSlot("location_mode")
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("Use profile location")
                .normalizedPrompt("Use profile location")
                .translationProvider("mock")
                .focusSlotId("location_mode")
                .focusSlotConfidence(1.0)
                .translationApplied(false)
                .translationReliable(true)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .location(VisionPromptUnderstandingLocationSlots.builder()
                                .mode("profile")
                                .modeConfidence(1.0)
                                .build())
                        .build())
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(conversation, "Use profile location", understanding);

        assertEquals("profile", merged.get("location_mode"));
        assertFalse(merged.containsKey("quest_description"));
    }

    @Test
    void rewardPromptDoesNotLeakIntoDescription() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .requestedSlot("reward_amount")
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("It is free")
                .normalizedPrompt("It is free")
                .translationProvider("mock")
                .focusSlotId("reward_amount")
                .focusSlotConfidence(1.0)
                .translationApplied(false)
                .translationReliable(true)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .reward(VisionPromptUnderstandingRewardSlots.builder()
                                .freeQuest(true)
                                .freeQuestConfidence(1.0)
                                .amount("0")
                                .amountConfidence(1.0)
                                .build())
                        .build())
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(conversation, "It is free", understanding);

        assertEquals("true", merged.get("free_quest"));
        assertEquals("0", merged.get("reward_amount"));
        assertFalse(merged.containsKey("quest_description"));
    }

    @Test
    void mixedCreateQuestPromptKeepsCoreTaskOutOfDescriptionRewardAndLocation() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser).build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("Move my sofa next Tuesday at 14:30 for 20 euros at Ilica 10")
                .normalizedPrompt("Move my sofa next Tuesday at 14:30 for 20 euros at Ilica 10")
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .semanticPlan(VisionSemanticPlan.createQuest(0.95d, "mock create quest"))
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(
                conversation,
                "Move my sofa next Tuesday at 14:30 for 20 euros at Ilica 10",
                understanding
        );

        assertEquals("Move my sofa", merged.get("quest_title"));
        assertEquals("Move my sofa", merged.get("quest_description"));
        assertEquals("20", merged.get("reward_amount"));
        assertEquals("false", merged.get("free_quest"));
        assertEquals("fixed", merged.get("schedule_mode"));
        assertEquals("custom", merged.get("location_mode"));
        assertFalse(merged.get("location_label").isBlank());
        assertTrue(merged.get("location_label").contains("Ilica 10"));
    }

    @Test
    void highConfidenceSemanticTitleOverwritesExistingDraftValue() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .slot("quest_title", "Old title")
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("New title")
                .normalizedPrompt("New title")
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .questTitle("New title")
                        .questTitleConfidence(0.9d)
                        .build())
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(conversation, "New title", understanding);

        assertEquals("New title", merged.get("quest_title"));
    }

    @Test
    void mediumConfidenceSemanticTitleDoesNotOverwriteExistingDraftValue() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .slot("quest_title", "Old title")
                .build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("New title")
                .normalizedPrompt("New title")
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .questTitle("New title")
                        .questTitleConfidence(0.60d)
                        .build())
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(conversation, "New title", understanding);

        assertEquals("Old title", merged.get("quest_title"));
    }

    @Test
    void semanticRewardAmountIsNormalizedBeforePersisting() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser).build();

        VisionPromptUnderstandingResult understanding = VisionPromptUnderstandingResult.builder()
                .sourceLanguage("en")
                .originalPrompt("twenty euros")
                .normalizedPrompt("twenty euros")
                .translationProvider("mock")
                .translationApplied(false)
                .translationReliable(true)
                .slots(VisionPromptUnderstandingSlots.builder()
                        .reward(VisionPromptUnderstandingRewardSlots.builder()
                                .freeQuest(false)
                                .freeQuestConfidence(0.9d)
                                .amount("20 EUR")
                                .amountConfidence(0.9d)
                                .build())
                        .build())
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(conversation, "twenty euros", understanding);

        assertEquals("false", merged.get("free_quest"));
        assertEquals("20", merged.get("reward_amount"));
    }

    @Test
    void locationCandidateConfirmationCanSelectSecondOption() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        LocationLookupService lookupService = Mockito.mock(LocationLookupService.class);
        VisionSlotService localSlotService = new VisionSlotService(
                new VisionScheduleParserService(),
                new VisionLocationResolutionService(new VisionLocationParserService(), lookupService),
                new VisionSemanticMapper()
        );

        VisionConversation candidateConversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .requestedSlot("location_label")
                .build();

        Mockito.when(lookupService.lookupTopCandidates("Ilica 10, Zagreb", "vision:user:7", 3))
                .thenReturn(VisionLocationCandidatePresets.dualIlicaCandidates());

        Map<String, String> firstMerge = localSlotService.mergeCreateQuestSlots(
                candidateConversation,
                "Ilica 10, Zagreb"
        );

        VisionConversation confirmationConversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .requestedSlot("location_candidate_confirmation")
                .build();
        confirmationConversation.setSlotData(new LinkedHashMap<>(firstMerge));

        Map<String, String> resolved = localSlotService.mergeCreateQuestSlots(
                confirmationConversation,
                "the second option"
        );

        assertEquals("lookup_resolved", resolved.get("location_resolution_status"));
        assertEquals("Ilica 10, 10000 Zagreb, Croatia", resolved.get("location_label"));
        assertFalse(resolved.containsKey("pending_location_candidate_count"));
    }

    @Test
    void locationParsingKeepsPrefixedPlaceLabelsReadable() {
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        VisionConversation conversation = VisionConversationTestBuilder.createQuest(1L, currentUser)
                .requestedSlot("location_label")
                .slot("location_mode", "custom")
                .build();

        Map<String, String> merged = visionSlotService.mergeCreateQuestSlots(
                conversation,
                "place: Ban Jelacic Square, Zagreb"
        );

        assertEquals("Ban Jelacic Square, Zagreb", merged.get("location_label"));
        assertEquals("Ban Jelacic Square", merged.get("location_street"));
        assertEquals("Zagreb", merged.get("location_locality"));
    }
}
