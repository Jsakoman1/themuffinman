package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionMemoryTrailDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
class VisionConversationReadModelAssembler {

    private final VisionConversationRepository visionConversationRepository;
    private final VisionTurnRepository visionTurnRepository;
    private final VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService;
    private final VisionClarificationService visionClarificationService;

    List<VisionConversationSummaryDTO> recentConversationSummaries(AppUser currentUser) {
        List<VisionConversation> recentConversations = visionConversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(currentUser);
        List<VisionConversationSummaryDTO> summaries = new ArrayList<>();
        String previousEntityFamily = null;
        for (VisionConversation conversation : recentConversations) {
            String entityFamily = VisionEntityFamilySupport.conversationFamilyLabel(conversation == null ? null : conversation.getIntent());
            summaries.add(toConversationSummary(conversation, entityFamily, previousEntityFamily));
            if (entityFamily != null && !entityFamily.isBlank()) {
                previousEntityFamily = entityFamily;
            }
        }
        return summaries;
    }

    VisionMemoryTrailDTO buildMemoryTrail(AppUser currentUser, VisionConversation conversation) {
        VisionSemanticMemoryContext memoryContext =
                visionSemanticOrchestrationContextService.buildMemoryContext(currentUser, conversation);
        String activeEntityFamily = memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getCurrentEntityFamily();
        String previousEntityFamily = previousEntityFamily(memoryContext.getUserMemory() == null ? List.of() : memoryContext.getUserMemory().getRecentEntityFamilies(), activeEntityFamily);
        return VisionMemoryTrailDTO.builder()
                .activeEntityFamily(activeEntityFamily)
                .previousEntityFamily(previousEntityFamily)
                .topicSwitchHint(topicSwitchHint(activeEntityFamily, previousEntityFamily))
                .currentIntent(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getCurrentIntent())
                .currentRequestedSlot(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getRequestedSlot())
                .currentStatus(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getStatus())
                .sessionSummary(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getSessionSummary())
                .lastUserPrompt(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getLastUserPrompt())
                .lastNormalizedPrompt(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getLastNormalizedPrompt())
                .lastAssistantMessage(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getLastAssistantMessage())
                .sessionMemorySnapshot(memoryContext.getSessionMemory() == null ? null : memoryContext.getSessionMemory().getSessionMemorySnapshot())
                .openQuestions(memoryContext.getSessionMemory() == null ? List.of() : memoryContext.getSessionMemory().getOpenQuestions())
                .recentActions(memoryContext.getSessionMemory() == null ? List.of() : memoryContext.getSessionMemory().getRecentActions())
                .recentEntityFamilies(memoryContext.getUserMemory() == null ? List.of() : memoryContext.getUserMemory().getRecentEntityFamilies())
                .recentIntentTypes(memoryContext.getUserMemory() == null ? List.of() : memoryContext.getUserMemory().getRecentIntentTypes())
                .build();
    }

    List<VisionSlotSummaryDTO> appliedSlotSummariesForTurn(VisionTurn turn) {
        if (turn == null || turn.getAppliedSlotIds() == null || turn.getAppliedSlotIds().isEmpty()) {
            return List.of();
        }

        Map<String, String> slotData = turn.getConversation().getSlotData();
        return turn.getAppliedSlotIds().stream()
                .map(slotId -> VisionSlotSummaryDTO.builder()
                        .slotId(slotId)
                        .label(labelForSlot(slotId))
                        .value(valueForSlot(slotData, slotId))
                        .build())
                .filter(summary -> summary.getLabel() != null && summary.getValue() != null && !summary.getValue().isBlank())
                .toList();
    }

    VisionConversationSummaryDTO toConversationSummary(VisionConversation conversation, String entityFamily, String previousEntityFamily) {
        String title = conversation.getSlotData().get("quest_title");
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("circle_name");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("resolved_circle_name");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("circle_request_target_username");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("application_quest_title");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("managed_application_quest_title");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("profile_username");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("profile_location_label");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("resolved_profile_username");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("resolved_quest_title");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getSlotData().get("search_query");
        }
        if ((title == null || title.isBlank()) && conversation.getIntent() == VisionIntent.VIEW_QUEST_NEWS) {
            title = "Quest news";
        }
        if ((title == null || title.isBlank()) && conversation.getIntent() == VisionIntent.VIEW_NOTIFICATIONS) {
            title = "Notifications";
        }
        if ((title == null || title.isBlank()) && conversation.getIntent() == VisionIntent.OPEN_CHAT) {
            title = conversation.getSlotData().get("opened_chat_username");
        }
        if ((title == null || title.isBlank()) && conversation.getIntent() == VisionIntent.OPEN_CHAT) {
            title = conversation.getSlotData().get("target_user");
        }
        if (title == null || title.isBlank()) {
            title = conversation.getIntent().name();
        }
        String subtitle = conversation.getLastAssistantMessage();
        if (subtitle == null || subtitle.isBlank()) {
            subtitle = conversation.getRequestedSlot() == null
                    ? "No pending prompt"
                    : "Waiting for " + conversation.getRequestedSlot();
        }
        boolean stale = isStale(conversation);
        boolean completed = conversation.getStatus() == VisionConversationStatus.COMPLETED;
        boolean resumable = !completed && !stale;
        String topicSwitchHint = topicSwitchHint(entityFamily, previousEntityFamily);
        List<VisionSlotSummaryDTO> appliedSlotSummaries = visionTurnRepository.findTopByConversationOrderByTurnIndexDesc(conversation)
                .map(this::appliedSlotSummariesForTurn)
                .orElseGet(List::of);
        return VisionConversationSummaryDTO.builder()
                .conversationId(conversation.getId())
                .intent(conversation.getIntent().name())
                .entityFamily(entityFamily)
                .previousEntityFamily(previousEntityFamily)
                .topicSwitchHint(topicSwitchHint)
                .status(conversation.getStatus().name())
                .title(title)
                .subtitle(subtitle)
                .stageLabel(VisionSurfaceModeSupport.stageLabelFor(conversation))
                .progressLabel(VisionSurfaceModeSupport.progressLabelFor(conversation, visionClarificationService))
                .groupKey(VisionSurfaceModeSupport.groupKeyFor(conversation.getStatus()))
                .requestedSlot(conversation.getRequestedSlot())
                .appliedSlotSummaries(appliedSlotSummaries)
                .resumable(resumable)
                .completed(completed)
                .stale(stale)
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private String previousEntityFamily(List<String> recentEntityFamilies, String activeEntityFamily) {
        if (recentEntityFamilies == null || recentEntityFamilies.isEmpty()) {
            return null;
        }

        for (String family : recentEntityFamilies) {
            if (family == null || family.isBlank()) {
                continue;
            }
            if (activeEntityFamily == null || !family.equals(activeEntityFamily)) {
                return family;
            }
        }
        return null;
    }

    private String topicSwitchHint(String activeEntityFamily, String previousEntityFamily) {
        if (activeEntityFamily == null || activeEntityFamily.isBlank() || previousEntityFamily == null || previousEntityFamily.isBlank()) {
            return null;
        }
        if (activeEntityFamily.equals(previousEntityFamily)) {
            return null;
        }
        return "Switched from " + previousEntityFamily + " to " + activeEntityFamily + ".";
    }

    private String labelForSlot(String slotId) {
        return switch (slotId) {
            case "quest_title" -> "Title";
            case "quest_description" -> "Description";
            case "target_quest_query" -> "Quest";
            case "target_circle_query" -> "Circle";
            case "target_application_query" -> "Application";
            case "target_user" -> "Person";
            case "application_message" -> "Application message";
            case "application_proposed_price" -> "Proposed price";
            case "application_existing_message" -> "Current message";
            case "application_existing_proposed_price" -> "Current price";
            case "profile_username" -> "Username";
            case "profile_description" -> "Profile description";
            case "profile_location_mode" -> "Location mode";
            case "profile_location_label" -> "Location";
            case "reward_amount" -> "Reward";
            case "visibility" -> "Visibility";
            case "schedule_mode" -> "Schedule";
            case "scheduled_date" -> "Date";
            case "scheduled_time" -> "Time";
            case "location_mode" -> "Location";
            case "location_label" -> "Custom place";
            case "location_candidate_confirmation" -> "Location confirmation";
            default -> slotId;
        };
    }

    private String valueForSlot(Map<String, String> slotData, String slotId) {
        if (slotData == null) {
            return "";
        }

        return switch (slotId) {
            case "reward_amount" -> "true".equals(slotData.get("free_quest")) ? "Free" : slotData.get("reward_amount");
            case "schedule_mode" -> {
                String mode = slotData.get("schedule_mode");
                if ("fixed".equals(mode)) {
                    yield "Fixed time";
                }
                if ("agreement".equals(mode)) {
                    yield "By agreement";
                }
                yield mode;
            }
            case "scheduled_date" -> slotData.get("scheduled_date");
            case "scheduled_time" -> slotData.get("scheduled_time");
            case "location_mode" -> {
                String mode = slotData.get("location_mode");
                if ("profile".equals(mode)) {
                    yield "Use profile location";
                }
                if ("off".equals(mode)) {
                    yield "Hide location";
                }
                if ("custom".equals(mode)) {
                    String label = slotData.get("location_label");
                    yield label == null || label.isBlank() ? "Custom place" : label;
                }
                yield mode;
            }
            case "target_quest_query" -> firstNonBlank(
                    slotData.get("application_quest_title"),
                    slotData.get("resolved_quest_title"),
                    slotData.get("target_quest_query")
            );
            case "target_circle_query" -> slotData.get("resolved_circle_name");
            case "target_application_query" -> firstNonBlank(
                    slotData.get("application_quest_title"),
                    slotData.get("target_application_query")
            );
            case "target_user" -> firstNonBlank(
                    slotData.get("managed_application_applicant_username"),
                    slotData.get("circle_request_target_username"),
                    slotData.get("opened_chat_username"),
                    slotData.get("resolved_profile_username"),
                    slotData.get("target_user")
            );
            case "application_message" -> slotData.get("application_message");
            case "application_proposed_price" -> slotData.get("application_proposed_price");
            case "application_existing_message" -> slotData.get("application_existing_message");
            case "application_existing_proposed_price" -> slotData.get("application_existing_proposed_price");
            case "profile_description" -> slotData.get("profile_description");
            case "profile_location_mode" -> slotData.get("profile_location_mode");
            case "profile_location_label" -> slotData.get("profile_location_label");
            default -> slotData.get(slotId);
        };
    }

    private boolean isStale(VisionConversation conversation) {
        Instant updatedAt = conversation.getUpdatedAt();
        if (updatedAt == null) {
            return false;
        }
        return Duration.between(updatedAt, Instant.now()).toHours() >= 24;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
