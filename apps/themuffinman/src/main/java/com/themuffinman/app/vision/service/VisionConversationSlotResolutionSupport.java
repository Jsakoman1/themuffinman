package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.vision.model.VisionConversation;

import java.util.List;

final class VisionConversationSlotResolutionSupport {

    private static final String SLOT_TARGET_QUEST_QUERY = "target_quest_query";
    private static final String SLOT_APPLICATION_MESSAGE = "application_message";
    private static final String SLOT_APPLICATION_PROPOSED_PRICE = "application_proposed_price";
    private static final String SLOT_CIRCLE_NAME = "circle_name";
    private static final String SLOT_APPLICATION_QUEST_ID = "application_quest_id";
    private static final String SLOT_APPLICATION_EXISTING_MESSAGE = "application_existing_message";
    private static final String SLOT_APPLICATION_EXISTING_PROPOSED_PRICE = "application_existing_proposed_price";
    private static final String SLOT_TARGET_USER = "target_user";
    private static final String SLOT_PROFILE_LOCATION_MODE = "profile_location_mode";
    private static final String SLOT_PROFILE_LOCATION_LABEL = "profile_location_label";
    private static final String SLOT_PROFILE_USERNAME = "profile_username";
    private static final String SLOT_PROFILE_DESCRIPTION = "profile_description";

    private static final List<String> APPLICATION_QUEST_PREFIXES = List.of(
            "apply to quest",
            "apply for quest",
            "apply to job",
            "apply for job",
            "apply to",
            "apply for",
            "create application for",
            "send application for"
    );
    private static final List<String> APPLICATION_MESSAGE_PREFIXES = List.of("my message is", "application message", "message");
    private static final List<String> CIRCLE_NAME_PREFIXES = List.of("create new circle", "create circle", "new circle", "make a circle", "make circle", "start a circle", "start circle", "circle");
    private static final List<String> CIRCLE_NAME_TRAILING_PREFIXES = List.of(" called ", " named ");
    private static final List<String> CIRCLE_RENAME_PREFIXES = List.of("rename to", "new name", "change name to", "change name");
    private static final List<String> CIRCLE_RENAME_TRAILING_PREFIXES = List.of(" to ");
    private static final List<String> MANAGED_APPLICANT_PREFIXES = List.of("approve application", "decline application", "reject application", "accept application", "applicant", "approve", "decline", "reject", "accept");
    private static final List<String> MANAGED_APPLICANT_TRAILING_PREFIXES = List.of(" for ");
    private static final List<String> USER_PROFILE_PREFIXES = List.of("show profile of", "open profile of", "show profile for", "open profile for", "show user", "open user");
    private static final List<String> CIRCLE_REQUEST_PREFIXES = List.of(
            "send a circle request to",
            "send circle request to",
            "invite to my circle",
            "invite to my circles",
            "add to my circle",
            "add to my circles",
            "connect with",
            "accept circle request from",
            "accept connection request from",
            "accept invite from",
            "decline circle request from",
            "reject circle request from",
            "decline invite from",
            "reject invite from",
            "cancel circle request to",
            "cancel invite to",
            "delete circle request with",
            "remove circle request with"
    );
    private static final List<String> CIRCLE_REQUEST_STRIP_WORDS = List.of("user", "users", "person", "people", "member", "members", "contact", "contacts", "profile", "profiles");
    private static final List<String> PROFILE_LOCATION_PREFIXES = List.of("set my location to", "update my location to", "change my location to", "location");
    private static final List<String> PROFILE_USERNAME_PREFIXES = List.of("update my username to", "change my username to", "set my username to", "my username is", "username");
    private static final List<String> PROFILE_DESCRIPTION_PREFIXES = List.of(
            "set my profile description to",
            "change my profile description to",
            "update my profile description to",
            "set my description to",
            "change my description to",
            "update my description to",
            "set description to",
            "set bio to",
            "change my bio to",
            "update my bio to"
    );
    private static final List<String> PROFILE_DESCRIPTION_INSTRUCTION_PREFIXES = List.of(
            "set description",
            "set my description",
            "change my description",
            "update my description",
            "set bio",
            "change my bio",
            "update my bio",
            "set my profile description",
            "change my profile description",
            "update my profile description"
    );

    private final VisionClarificationService visionClarificationService;

    VisionConversationSlotResolutionSupport(VisionClarificationService visionClarificationService) {
        this.visionClarificationService = visionClarificationService;
    }

    String resolveApplicationQuestQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_TARGET_QUEST_QUERY)) {
            return semanticSlotValue(understanding, SLOT_TARGET_QUEST_QUERY).trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && SLOT_TARGET_QUEST_QUERY.equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            APPLICATION_QUEST_PREFIXES
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                APPLICATION_QUEST_PREFIXES
        );
    }

    String resolveApplicationMessage(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_APPLICATION_MESSAGE)) {
            return semanticSlotValue(understanding, SLOT_APPLICATION_MESSAGE).trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && SLOT_APPLICATION_MESSAGE.equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            APPLICATION_MESSAGE_PREFIXES
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                APPLICATION_MESSAGE_PREFIXES
        );
    }

    String resolveApplicationProposedPrice(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_APPLICATION_PROPOSED_PRICE)) {
            return normalizeApplicationPrice(semanticSlotValue(understanding, SLOT_APPLICATION_PROPOSED_PRICE));
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        if (conversation != null && SLOT_APPLICATION_PROPOSED_PRICE.equals(conversation.getRequestedSlot())) {
            return normalizeApplicationPrice(normalizedPrompt);
        }
        return null;
    }

    String resolveCircleName(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_CIRCLE_NAME)) {
            return semanticSlotValue(understanding, SLOT_CIRCLE_NAME).trim();
        }

        if (conversation != null && SLOT_CIRCLE_NAME.equals(conversation.getRequestedSlot())) {
            return firstNonBlank(extractCircleNameFromPrompt(normalizedPrompt), normalizedPrompt == null ? null : normalizedPrompt.trim());
        }

        return extractCircleNameFromPrompt(normalizedPrompt);
    }

    String resolveCircleRename(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_CIRCLE_NAME)) {
            return semanticSlotValue(understanding, SLOT_CIRCLE_NAME).trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && SLOT_CIRCLE_NAME.equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            CIRCLE_RENAME_PREFIXES,
                            CIRCLE_RENAME_TRAILING_PREFIXES
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                CIRCLE_RENAME_PREFIXES,
                CIRCLE_RENAME_TRAILING_PREFIXES
        );
    }

    String nextMissingUpdateApplicationSlot(VisionConversation conversation) {
        if (conversation == null) {
            return SLOT_TARGET_QUEST_QUERY;
        }
        if (!hasText(conversation.getSlotData().get(SLOT_APPLICATION_QUEST_ID))) {
            return SLOT_TARGET_QUEST_QUERY;
        }
        if (!hasText(conversation.getSlotData().get(SLOT_APPLICATION_MESSAGE))
                && !hasText(conversation.getSlotData().get(SLOT_APPLICATION_PROPOSED_PRICE))) {
            return SLOT_APPLICATION_MESSAGE;
        }
        return null;
    }

    String questionForUpdateApplication(String slotId) {
        if (SLOT_APPLICATION_MESSAGE.equals(slotId)) {
            return "What should I change in your application? You can give a new message, a new price, or both.";
        }
        return visionClarificationService.buildQuestion(slotId);
    }

    String retryQuestionForUpdateApplication(String slotId) {
        if (SLOT_APPLICATION_MESSAGE.equals(slotId)) {
            return "I still need at least one application change. Say a new message, a new price, or both.";
        }
        return visionClarificationService.buildRetryQuestion(slotId);
    }

    String effectiveApplicationMessage(VisionConversation conversation) {
        String draftMessage = conversation.getSlotData().get(SLOT_APPLICATION_MESSAGE);
        if (hasText(draftMessage)) {
            return draftMessage;
        }
        return conversation.getSlotData().get(SLOT_APPLICATION_EXISTING_MESSAGE);
    }

    String effectiveApplicationPrice(VisionConversation conversation) {
        String draftPrice = conversation.getSlotData().get(SLOT_APPLICATION_PROPOSED_PRICE);
        if (hasText(draftPrice)) {
            return draftPrice;
        }
        return conversation.getSlotData().get(SLOT_APPLICATION_EXISTING_PROPOSED_PRICE);
    }

    String resolveManagedApplicantQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.semanticPlanOrEmpty().getTargetUserQuery();
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && SLOT_TARGET_USER.equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            MANAGED_APPLICANT_PREFIXES,
                            MANAGED_APPLICANT_TRAILING_PREFIXES
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                MANAGED_APPLICANT_PREFIXES,
                MANAGED_APPLICANT_TRAILING_PREFIXES
        );
    }

    String resolveUserProfileQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.semanticPlanOrEmpty().getTargetUserQuery();
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && SLOT_TARGET_USER.equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            USER_PROFILE_PREFIXES
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                USER_PROFILE_PREFIXES
        );
    }

    String resolveCircleRequestTargetUser(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        String semanticValue = understanding == null ? null : understanding.semanticPlanOrEmpty().getTargetUserQuery();
        if (semanticValue != null && !semanticValue.isBlank()) {
            return semanticValue.trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && SLOT_TARGET_USER.equals(conversation.getRequestedSlot())) {
            return VisionPromptTextSupport.stripLeadingWords(
                    firstNonBlank(
                            VisionPromptTextSupport.extractAfterAnyPrefix(
                                    trimmed,
                                    CIRCLE_REQUEST_PREFIXES
                            ),
                            trimmed
                    ),
                    CIRCLE_REQUEST_STRIP_WORDS
            );
        }
        String stripped = VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                CIRCLE_REQUEST_PREFIXES
        );
        return VisionPromptTextSupport.stripLeadingWords(
                stripped,
                CIRCLE_REQUEST_STRIP_WORDS
        );
    }

    String resolveProfileLocationMode(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_PROFILE_LOCATION_MODE)) {
            return TextValueNormalizer.upperTrimToEmpty(semanticSlotValue(understanding, SLOT_PROFILE_LOCATION_MODE));
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String lower = TextValueNormalizer.lowerTrimToEmpty(normalizedPrompt);
        if (conversation != null && SLOT_PROFILE_LOCATION_MODE.equals(conversation.getRequestedSlot())) {
            if (lower.contains("off") || lower.contains("hide")) {
                return "OFF";
            }
            if (lower.contains("approx")) {
                return "APPROXIMATE";
            }
            if (lower.contains("exact")) {
                return "EXACT";
            }
        }
        if (lower.contains("turn off") || lower.contains("hide my location") || lower.contains("location off")) {
            return "OFF";
        }
        if (lower.contains("approximate")) {
            return "APPROXIMATE";
        }
        if (lower.contains("exact")) {
            return "EXACT";
        }
        return null;
    }

    String resolveProfileLocationLabel(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_PROFILE_LOCATION_LABEL)) {
            return semanticSlotValue(understanding, SLOT_PROFILE_LOCATION_LABEL).trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        if (conversation != null && SLOT_PROFILE_LOCATION_LABEL.equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            normalizedPrompt,
                            PROFILE_LOCATION_PREFIXES
                    ),
                    normalizedPrompt.trim()
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                normalizedPrompt,
                PROFILE_LOCATION_PREFIXES
        );
    }

    String nextMissingProfileLocationSlot(VisionConversation conversation) {
        if (conversation == null) {
            return SLOT_PROFILE_LOCATION_MODE;
        }
        String mode = conversation.getSlotData().get(SLOT_PROFILE_LOCATION_MODE);
        if (!hasText(mode)) {
            return SLOT_PROFILE_LOCATION_MODE;
        }
        if (!"OFF".equalsIgnoreCase(mode)) {
            String draftLabel = conversation.getSlotData().get(SLOT_PROFILE_LOCATION_LABEL);
            if (!hasText(draftLabel)) {
                return SLOT_PROFILE_LOCATION_LABEL;
            }
        }
        return null;
    }

    String resolveProfileUsername(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_PROFILE_USERNAME)) {
            return semanticSlotValue(understanding, SLOT_PROFILE_USERNAME).trim();
        }

        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }

        String trimmed = normalizedPrompt.trim();
        if (conversation != null && SLOT_PROFILE_USERNAME.equals(conversation.getRequestedSlot())
                && !looksLikeProfileDescriptionInstruction(trimmed)
                && !looksLikeGenericProfileUpdateInstruction(trimmed)) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            PROFILE_USERNAME_PREFIXES
                    ),
                    trimmed
            );
        }

        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                PROFILE_USERNAME_PREFIXES
        );
    }

    String resolveProfileDescription(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, SLOT_PROFILE_DESCRIPTION)) {
            return semanticSlotValue(understanding, SLOT_PROFILE_DESCRIPTION).trim();
        }

        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }

        String trimmed = normalizedPrompt.trim();
        if (conversation != null && SLOT_PROFILE_DESCRIPTION.equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            PROFILE_DESCRIPTION_PREFIXES
                    ),
                    trimmed
            );
        }

        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                PROFILE_DESCRIPTION_PREFIXES
        );
    }

    boolean hasProfileUpdateDraft(VisionConversation conversation) {
        if (conversation == null || conversation.getSlotData() == null) {
            return false;
        }
        return hasText(conversation.getSlotData().get(SLOT_PROFILE_USERNAME))
                || conversation.getSlotData().containsKey(SLOT_PROFILE_DESCRIPTION);
    }

    boolean looksLikeProfileDescriptionInstruction(String prompt) {
        if (prompt == null) {
            return false;
        }
        String lower = TextValueNormalizer.lowerTrimToEmpty(prompt);
        return PROFILE_DESCRIPTION_INSTRUCTION_PREFIXES.stream().anyMatch(lower::startsWith);
    }

    boolean looksLikeGenericProfileUpdateInstruction(String prompt) {
        if (prompt == null) {
            return false;
        }
        String lower = TextValueNormalizer.lowerTrimToEmpty(prompt);
        return lower.equals("update my profile")
                || lower.equals("edit my profile")
                || lower.equals("change my profile");
    }

    boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean shouldUseSemanticSlotValue(
            VisionConversation conversation,
            VisionPromptUnderstandingResult understanding,
            String slotId
    ) {
        String value = semanticSlotValue(understanding, slotId);
        Double confidence = semanticSlotConfidence(understanding, slotId);
        if (!hasText(value) || confidence == null) {
            return false;
        }
        if (confidence >= VisionPromptUnderstandingResult.MIN_SLOT_CONFIDENCE) {
            return true;
        }
        if (confidence >= 0.45d) {
            java.util.Map<String, String> slotData = conversation == null ? null : conversation.getSlotData();
            return slotData == null || !hasText(slotData.get(slotId));
        }
        return false;
    }

    private String semanticSlotValue(VisionPromptUnderstandingResult understanding, String slotId) {
        return understanding == null ? null : understanding.slotValue(slotId);
    }

    private Double semanticSlotConfidence(VisionPromptUnderstandingResult understanding, String slotId) {
        return understanding == null ? null : understanding.slotConfidence(slotId);
    }

    private String extractCircleNameFromPrompt(String normalizedPrompt) {
        String stripped = VisionPromptTextSupport.extractAfterAnyPrefix(
                normalizedPrompt,
                List.of(
                        "create new circle",
                        "create circle",
                        "new circle",
                        "make a circle",
                        "make circle",
                        "start a circle",
                        "start circle",
                        "circle"
                ),
                List.of(" called ", " named ")
        );
        return VisionPromptTextSupport.stripLeadingWords(stripped, List.of("called", "named"));
    }

    private String normalizeApplicationPrice(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim()
                .replaceAll("(?i)\\s*(euros?|eur|chf|francs?)\\s*", "")
                .replace(',', '.')
                .trim();
        if (!normalized.matches("\\d+(\\.\\d{1,2})?")) {
            return null;
        }
        return normalized;
    }

    String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
