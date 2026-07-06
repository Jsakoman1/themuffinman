package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;

import java.util.List;
import java.util.Locale;

final class VisionConversationSlotResolutionSupport {

    private final VisionClarificationService visionClarificationService;

    VisionConversationSlotResolutionSupport(VisionClarificationService visionClarificationService) {
        this.visionClarificationService = visionClarificationService;
    }

    String resolveApplicationQuestQuery(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "target_quest_query")) {
            return semanticSlotValue(understanding, "target_quest_query").trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "target_quest_query".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of(
                                    "apply to quest",
                                    "apply for quest",
                                    "apply to job",
                                    "apply for job",
                                    "apply to",
                                    "apply for",
                                    "create application for",
                                    "send application for"
                            )
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of(
                        "apply to quest",
                        "apply for quest",
                        "apply to job",
                        "apply for job",
                        "apply to",
                        "apply for",
                        "create application for",
                        "send application for"
                )
        );
    }

    String resolveApplicationMessage(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "application_message")) {
            return semanticSlotValue(understanding, "application_message").trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "application_message".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of("my message is", "application message", "message")
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of("my message is", "application message", "message")
        );
    }

    String resolveApplicationProposedPrice(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "application_proposed_price")) {
            return normalizeApplicationPrice(semanticSlotValue(understanding, "application_proposed_price"));
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        if (conversation != null && "application_proposed_price".equals(conversation.getRequestedSlot())) {
            return normalizeApplicationPrice(normalizedPrompt);
        }
        return null;
    }

    String resolveCircleName(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "circle_name")) {
            return semanticSlotValue(understanding, "circle_name").trim();
        }

        if (conversation != null && "circle_name".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(extractCircleNameFromPrompt(normalizedPrompt), normalizedPrompt == null ? null : normalizedPrompt.trim());
        }

        return extractCircleNameFromPrompt(normalizedPrompt);
    }

    String resolveCircleRename(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "circle_name")) {
            return semanticSlotValue(understanding, "circle_name").trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "circle_name".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of("rename to", "new name", "change name to", "change name"),
                            List.of(" to ")
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of("rename to", "new name", "change name to", "change name"),
                List.of(" to ")
        );
    }

    String nextMissingUpdateApplicationSlot(VisionConversation conversation) {
        if (conversation == null) {
            return "target_quest_query";
        }
        if (!hasText(conversation.getSlotData().get("application_quest_id"))) {
            return "target_quest_query";
        }
        if (!hasText(conversation.getSlotData().get("application_message"))
                && !hasText(conversation.getSlotData().get("application_proposed_price"))) {
            return "application_message";
        }
        return null;
    }

    String questionForUpdateApplication(String slotId) {
        if ("application_message".equals(slotId)) {
            return "What should I change in your application? You can give a new message, a new price, or both.";
        }
        return visionClarificationService.buildQuestion(slotId);
    }

    String retryQuestionForUpdateApplication(String slotId) {
        if ("application_message".equals(slotId)) {
            return "I still need at least one application change. Say a new message, a new price, or both.";
        }
        return visionClarificationService.buildRetryQuestion(slotId);
    }

    String effectiveApplicationMessage(VisionConversation conversation) {
        String draftMessage = conversation.getSlotData().get("application_message");
        if (hasText(draftMessage)) {
            return draftMessage;
        }
        return conversation.getSlotData().get("application_existing_message");
    }

    String effectiveApplicationPrice(VisionConversation conversation) {
        String draftPrice = conversation.getSlotData().get("application_proposed_price");
        if (hasText(draftPrice)) {
            return draftPrice;
        }
        return conversation.getSlotData().get("application_existing_proposed_price");
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
        if (conversation != null && "target_user".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of(
                                    "approve application",
                                    "decline application",
                                    "reject application",
                                    "accept application",
                                    "applicant",
                                    "approve",
                                    "decline",
                                    "reject",
                                    "accept"
                            ),
                            List.of(" for ")
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of(
                        "approve application",
                        "decline application",
                        "reject application",
                        "accept application",
                        "applicant",
                        "approve",
                        "decline",
                        "reject",
                        "accept"
                ),
                List.of(" for ")
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
        if (conversation != null && "target_user".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of(
                                    "show profile of",
                                    "open profile of",
                                    "show profile for",
                                    "open profile for",
                                    "show user",
                                    "open user"
                            )
                    ),
                    trimmed
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of(
                        "show profile of",
                        "open profile of",
                        "show profile for",
                        "open profile for",
                        "show user",
                        "open user"
                )
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
        if (conversation != null && "target_user".equals(conversation.getRequestedSlot())) {
            return VisionPromptTextSupport.stripLeadingWords(
                    firstNonBlank(
                            VisionPromptTextSupport.extractAfterAnyPrefix(
                                    trimmed,
                                    List.of(
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
                                    )
                            ),
                            trimmed
                    ),
                    List.of("user", "users", "person", "people", "member", "members", "contact", "contacts", "profile", "profiles")
            );
        }
        String stripped = VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of(
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
                )
        );
        return VisionPromptTextSupport.stripLeadingWords(
                stripped,
                List.of("user", "users", "person", "people", "member", "members", "contact", "contacts", "profile", "profiles")
        );
    }

    String resolveProfileLocationMode(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "profile_location_mode")) {
            return semanticSlotValue(understanding, "profile_location_mode").trim().toUpperCase(Locale.ROOT);
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        String lower = normalizedPrompt.trim().toLowerCase(Locale.ROOT);
        if (conversation != null && "profile_location_mode".equals(conversation.getRequestedSlot())) {
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
        if (shouldUseSemanticSlotValue(conversation, understanding, "profile_location_label")) {
            return semanticSlotValue(understanding, "profile_location_label").trim();
        }
        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }
        if (conversation != null && "profile_location_label".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            normalizedPrompt,
                            List.of("set my location to", "update my location to", "change my location to", "location")
                    ),
                    normalizedPrompt.trim()
            );
        }
        return VisionPromptTextSupport.extractAfterAnyPrefix(
                normalizedPrompt,
                List.of("set my location to", "update my location to", "change my location to", "location")
        );
    }

    String nextMissingProfileLocationSlot(VisionConversation conversation) {
        if (conversation == null) {
            return "profile_location_mode";
        }
        String mode = conversation.getSlotData().get("profile_location_mode");
        if (!hasText(mode)) {
            return "profile_location_mode";
        }
        if (!"OFF".equalsIgnoreCase(mode)) {
            String draftLabel = conversation.getSlotData().get("profile_location_label");
            if (!hasText(draftLabel)) {
                return "profile_location_label";
            }
        }
        return null;
    }

    String resolveProfileUsername(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "profile_username")) {
            return semanticSlotValue(understanding, "profile_username").trim();
        }

        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }

        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "profile_username".equals(conversation.getRequestedSlot())
                && !looksLikeProfileDescriptionInstruction(trimmed)
                && !looksLikeGenericProfileUpdateInstruction(trimmed)) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of("update my username to", "change my username to", "set my username to", "my username is", "username")
                    ),
                    trimmed
            );
        }

        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of("update my username to", "change my username to", "set my username to", "my username is", "username")
        );
    }

    String resolveProfileDescription(
            VisionConversation conversation,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding
    ) {
        if (shouldUseSemanticSlotValue(conversation, understanding, "profile_description")) {
            return semanticSlotValue(understanding, "profile_description").trim();
        }

        if (normalizedPrompt == null || normalizedPrompt.isBlank()) {
            return null;
        }

        String trimmed = normalizedPrompt.trim();
        if (conversation != null && "profile_description".equals(conversation.getRequestedSlot())) {
            return firstNonBlank(
                    VisionPromptTextSupport.extractAfterAnyPrefix(
                            trimmed,
                            List.of(
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
                            )
                    ),
                    trimmed
            );
        }

        return VisionPromptTextSupport.extractAfterAnyPrefix(
                trimmed,
                List.of(
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
                )
        );
    }

    boolean hasProfileUpdateDraft(VisionConversation conversation) {
        if (conversation == null || conversation.getSlotData() == null) {
            return false;
        }
        return hasText(conversation.getSlotData().get("profile_username"))
                || conversation.getSlotData().containsKey("profile_description");
    }

    boolean looksLikeProfileDescriptionInstruction(String prompt) {
        if (prompt == null) {
            return false;
        }
        String lower = prompt.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("set description")
                || lower.startsWith("set my description")
                || lower.startsWith("change my description")
                || lower.startsWith("update my description")
                || lower.startsWith("set bio")
                || lower.startsWith("change my bio")
                || lower.startsWith("update my bio")
                || lower.startsWith("set my profile description")
                || lower.startsWith("change my profile description")
                || lower.startsWith("update my profile description");
    }

    boolean looksLikeGenericProfileUpdateInstruction(String prompt) {
        if (prompt == null) {
            return false;
        }
        String lower = prompt.trim().toLowerCase(Locale.ROOT);
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
