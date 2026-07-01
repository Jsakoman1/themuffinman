package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionCanvasBlockDTO;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionExecutionCandidateDTO;
import com.themuffinman.app.vision.dto.VisionOptionDTO;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionQuestReviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionTurn;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VisionCanvasAssembler {
    private static final DateTimeFormatter REVIEW_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final VisionProperties visionProperties;

    public VisionCanvasAssembler(VisionProperties visionProperties) {
        this.visionProperties = visionProperties;
    }

    public VisionConversationTurnResponseDTO assemble(
            VisionConversation conversation,
            VisionTurn turn,
            List<VisionConversationSummaryDTO> recentConversations,
            VisionExecutionCandidateDTO executionCandidate,
            VisionQuestDiscoveryDTO questDiscovery
    ) {
        return VisionConversationTurnResponseDTO.builder()
                .conversationId(conversation.getId())
                .turnId(turn.getId())
                .intent(conversation.getIntent().name())
                .agentState(turn.getAgentState().name())
                .canvasMode(toCanvasMode(turn))
                .nextAction(turn.getNextAction().name())
                .message(turn.getAssistantMessage())
                .requestedSlot(turn.getRequestedSlot())
                .normalizedPrompt(turn.getNormalizedPrompt())
                .translationApplied(turn.isTranslationApplied())
                .translationReliable(turn.isTranslationReliable())
                .executionEnabled(visionProperties.isExecutionEnabled())
                .executionCandidate(executionCandidate)
                .questDiscovery(questDiscovery)
                .blocks(toBlocks(conversation, turn, questDiscovery))
                .appliedSlotSummaries(toAppliedSlotSummaries(conversation.getSlotData(), turn.getAppliedSlotIds()))
                .slotSummaries(toSlotSummaries(conversation.getSlotData()))
                .review(toReview(conversation.getSlotData(), turn))
                .recentConversations(recentConversations)
                .build();
    }

    private String toCanvasMode(VisionTurn turn) {
        return switch (turn.getNextAction()) {
            case ASK_FOR_SLOT -> "clarification";
            case SHOW_REVIEW -> "review";
            case SHOW_RESULTS -> "results";
            case COMPLETE -> "complete";
            case BLOCKED -> "blocked";
        };
    }

    private List<VisionCanvasBlockDTO> toBlocks(VisionConversation conversation, VisionTurn turn, VisionQuestDiscoveryDTO questDiscovery) {
        List<VisionCanvasBlockDTO> blocks = new ArrayList<>();
        blocks.add(VisionCanvasBlockDTO.builder()
                .type("agent_message")
                .body(turn.getAssistantMessage())
                .tone(turn.getAgentState() == com.themuffinman.app.vision.model.VisionAgentState.BLOCKED ? "warning" : "neutral")
                .build());

        blocks.add(VisionCanvasBlockDTO.builder()
                .type("recognized_prompt")
                .title("Recognized input")
                .body(turn.getNormalizedPrompt())
                .build());

        if (!turn.isTranslationReliable()) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("warning")
                    .title("Translation check")
                    .body("Translation reliability dropped for this turn, so review the wording carefully.")
                    .tone("warning")
                    .build());
        }

        if (questDiscovery != null) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("quest_discovery")
                    .title("Quest discovery")
                    .body(questDiscovery.getSummary())
                    .questDiscovery(questDiscovery)
                    .tone("info")
                    .build());
        }

        List<VisionSlotSummaryDTO> summaries = toSlotSummaries(conversation.getSlotData());
        if (!summaries.isEmpty()) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("result_summary")
                    .title("Collected so far")
                    .items(summaries)
                    .build());
        }

        VisionCanvasBlockDTO locationResolutionBlock = toLocationResolutionBlock(conversation.getSlotData());
        if (locationResolutionBlock != null) {
            blocks.add(locationResolutionBlock);
        }

        if (turn.getRequestedSlot() != null && turn.getNextAction() == com.themuffinman.app.vision.model.VisionNextAction.ASK_FOR_SLOT) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("field_request")
                    .title(labelForSlot(turn.getRequestedSlot()))
                    .body(turn.getAssistantMessage())
                    .fieldId(turn.getRequestedSlot())
                    .fieldKind(kindForSlot(turn.getRequestedSlot()))
                    .required(true)
                    .placeholder(placeholderForSlot(turn.getRequestedSlot()))
                    .options(optionsForSlot(turn.getRequestedSlot(), conversation.getSlotData()))
                    .build());
        }

        VisionQuestReviewDTO review = toReview(conversation.getSlotData(), turn);
        if (review != null) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("review_summary")
                    .title("Quest review")
                    .body(visionProperties.isExecutionEnabled()
                            ? "Review the collected quest data before confirmation."
                            : "Execution is still disabled, so this phase stops at review.")
                    .review(review)
                    .tone(visionProperties.isExecutionEnabled() ? "neutral" : "info")
                    .build());
        }

        if (turn.getNextAction() == com.themuffinman.app.vision.model.VisionNextAction.COMPLETE) {
            if ("cancelled".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("warning")
                        .title("Task cancelled")
                        .body("The current vision task was cancelled before execution.")
                        .tone("info")
                        .build());
                return blocks;
            }
            String createdQuestId = conversation.getSlotData().get("created_quest_id");
            String createdQuestTitle = conversation.getSlotData().get("quest_title");
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("success")
                    .title("Quest created")
                    .body(createdQuestId == null
                            ? "The quest was created successfully."
                            : "Created quest #" + createdQuestId + " for " + createdQuestTitle + ".")
                    .tone("success")
                    .build());
        }

        return blocks;
    }

    private List<VisionSlotSummaryDTO> toSlotSummaries(Map<String, String> slotData) {
        List<VisionSlotSummaryDTO> summaries = new ArrayList<>();
        addSummary(summaries, "quest_title", "Title", slotData.get("quest_title"));
        addSummary(summaries, "quest_description", "Description", slotData.get("quest_description"));
        if ("true".equals(slotData.get("free_quest"))) {
            addSummary(summaries, "reward_amount", "Reward", "Free");
        } else {
            addSummary(summaries, "reward_amount", "Reward", slotData.get("reward_amount"));
        }
        addSummary(summaries, "visibility", "Visibility", slotData.get("visibility"));
        addSummary(summaries, "schedule_mode", "Schedule", formatScheduleSummary(slotData));
        addSummary(summaries, "scheduled_date", "Date", formatScheduledDate(slotData.get("scheduled_date")));
        addSummary(summaries, "scheduled_time", "Time", formatScheduledTime(slotData.get("scheduled_time")));
        addSummary(summaries, "location_mode", "Location", formatLocationSummary(slotData));
        return summaries;
    }

    private List<VisionSlotSummaryDTO> toAppliedSlotSummaries(Map<String, String> slotData, List<String> appliedSlotIds) {
        List<VisionSlotSummaryDTO> summaries = new ArrayList<>();
        if (appliedSlotIds == null || appliedSlotIds.isEmpty()) {
            return summaries;
        }

        for (String slotId : appliedSlotIds) {
            String label = labelForSlot(slotId);
            String value = valueForSlot(slotData, slotId);
            if (label == null || value == null || value.isBlank()) {
                continue;
            }
            summaries.add(VisionSlotSummaryDTO.builder()
                    .slotId(slotId)
                    .label(label)
                    .value(value)
                    .build());
        }
        return summaries;
    }

    private VisionCanvasBlockDTO toLocationResolutionBlock(Map<String, String> slotData) {
        if (!"custom".equals(slotData.get("location_mode"))) {
            return null;
        }

        String status = slotData.get("location_resolution_status");
        if ("lookup_resolved".equals(status)) {
            String provider = slotData.get("location_resolution_provider");
            return VisionCanvasBlockDTO.builder()
                    .type("info")
                    .title("Location resolved")
                    .body(provider == null || provider.isBlank()
                            ? "The backend matched your custom location to a resolved place candidate."
                            : "The backend matched your custom location through " + provider + " and enriched the address details.")
                    .tone("info")
                    .build();
        }

        if ("candidate_pending".equals(status)) {
            String typedLabel = slotData.get("location_label");
            String resolvedLabel = slotData.get("pending_location_candidate_1_label");
            String topMatchNote = slotData.get("pending_location_candidate_1_match_note");
            String rawCount = slotData.get("pending_location_candidate_count");
            int candidateCount = 1;
            try {
                candidateCount = rawCount == null ? 1 : Integer.parseInt(rawCount);
            } catch (NumberFormatException ignored) {
                candidateCount = 1;
            }
            return VisionCanvasBlockDTO.builder()
                    .type("info")
                    .title("Location match found")
                    .body("The backend found "
                            + (candidateCount > 1 ? candidateCount + " location candidates" : "a more precise match")
                            + (resolvedLabel == null || resolvedLabel.isBlank() ? "." : ": " + resolvedLabel + ".")
                            + (topMatchNote == null || topMatchNote.isBlank() ? "" : " " + topMatchNote)
                            + (typedLabel == null || typedLabel.isBlank() ? "" : " Your typed location was " + typedLabel + ".")
                            + " You can also keep the typed location if none of the candidates is correct.")
                    .tone("info")
                    .build();
        }

        if ("parsed_only".equals(status)) {
            return VisionCanvasBlockDTO.builder()
                    .type("info")
                    .title("Location kept as typed")
                    .body("The backend kept your custom location from the typed text because no lookup candidate was applied.")
                    .tone("neutral")
                    .build();
        }

        return null;
    }

    private VisionQuestReviewDTO toReview(Map<String, String> slotData, VisionTurn turn) {
        if (!"SHOW_REVIEW".equals(turn.getNextAction().name())) {
            return null;
        }
        String rewardLabel = "true".equals(slotData.get("free_quest"))
                ? "Free"
                : slotData.get("reward_amount");
        return VisionQuestReviewDTO.builder()
                .title(slotData.get("quest_title"))
                .description(slotData.get("quest_description"))
                .rewardLabel(rewardLabel)
                .visibility(slotData.get("visibility"))
                .schedule(formatScheduleSummary(slotData))
                .location(formatLocationSummary(slotData))
                .build();
    }

    private void addSummary(List<VisionSlotSummaryDTO> summaries, String slotId, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        summaries.add(VisionSlotSummaryDTO.builder()
                .slotId(slotId)
                .label(label)
                .value(value)
                .build());
    }

    private String valueForSlot(Map<String, String> slotData, String slotId) {
        return switch (slotId) {
            case "reward_amount" -> "true".equals(slotData.get("free_quest")) ? "Free" : slotData.get("reward_amount");
            case "schedule_mode" -> formatScheduleSummary(slotData);
            case "scheduled_date" -> formatScheduledDate(slotData.get("scheduled_date"));
            case "scheduled_time" -> formatScheduledTime(slotData.get("scheduled_time"));
            case "location_mode" -> formatLocationSummary(slotData);
            default -> slotData.get(slotId);
        };
    }

    private String labelForSlot(String slotId) {
        return switch (slotId) {
            case "quest_title" -> "Title";
            case "quest_description" -> "Description";
            case "reward_amount" -> "Reward";
            case "visibility" -> "Visibility";
            case "schedule_mode" -> "Schedule";
            case "scheduled_date" -> "Date";
            case "scheduled_time" -> "Time";
            case "location_mode" -> "Location";
            case "location_label" -> "Custom place";
            case "location_candidate_confirmation" -> "Location confirmation";
            default -> "Next field";
        };
    }

    private String kindForSlot(String slotId) {
        return switch (slotId) {
            case "quest_description" -> "long_text";
            case "reward_amount" -> "money";
            case "visibility" -> "single_choice";
            case "schedule_mode", "location_mode" -> "single_choice";
            case "location_candidate_confirmation" -> "single_choice";
            case "scheduled_date" -> "date";
            case "scheduled_time" -> "time";
            default -> "short_text";
        };
    }

    private String placeholderForSlot(String slotId) {
        return switch (slotId) {
            case "quest_title" -> "Name the quest in a few words";
            case "quest_description" -> "Describe the task clearly";
            case "reward_amount" -> "Example: 20 euros or free";
            case "visibility" -> "Choose who should see the quest";
            case "schedule_mode" -> "Choose fixed time or by agreement";
            case "scheduled_date" -> "Example: 2026-07-03 or next Tuesday";
            case "scheduled_time" -> "Example: 14:30 or 2 pm";
            case "location_mode" -> "Choose profile, hidden, or custom";
            case "location_label" -> "Example: Main square in Zurich";
            case "location_candidate_confirmation" -> "Choose resolved place or keep typed location";
            default -> "Type the next detail";
        };
    }

    private List<VisionOptionDTO> optionsForSlot(String slotId, Map<String, String> slotData) {
        return switch (slotId) {
            case "visibility" -> List.of(
                    VisionOptionDTO.builder().id("PUBLIC").label("Public").value("PUBLIC").build(),
                    VisionOptionDTO.builder().id("CIRCLES").label("Circles only").value("CIRCLES").build()
            );
            case "schedule_mode" -> List.of(
                    VisionOptionDTO.builder().id("fixed").label("Fixed time").value("fixed").build(),
                    VisionOptionDTO.builder().id("agreement").label("By agreement").value("agreement").build()
            );
            case "location_mode" -> List.of(
                    VisionOptionDTO.builder().id("profile").label("Use profile").value("profile").build(),
                    VisionOptionDTO.builder().id("off").label("Hide location").value("off").build(),
                    VisionOptionDTO.builder().id("custom").label("Custom place").value("custom").build()
            );
            case "location_candidate_confirmation" -> locationCandidateOptions(slotData);
            default -> List.of();
        };
    }

    private List<VisionOptionDTO> locationCandidateOptions(Map<String, String> slotData) {
        List<VisionOptionDTO> options = new ArrayList<>();
        int count = 0;
        try {
            count = Integer.parseInt(slotData.getOrDefault("pending_location_candidate_count", "0"));
        } catch (NumberFormatException ignored) {
            count = 0;
        }
        for (int index = 1; index <= count; index++) {
            String label = slotData.get("pending_location_candidate_" + index + "_label");
            if (label == null || label.isBlank()) {
                continue;
            }
            String matchNote = slotData.get("pending_location_candidate_" + index + "_match_note");
            options.add(VisionOptionDTO.builder()
                    .id("candidate_" + index)
                    .label(matchNote == null || matchNote.isBlank()
                            ? "Candidate " + index + ": " + label
                            : "Candidate " + index + ": " + label + " (" + matchNote + ")")
                    .value("candidate " + index)
                    .build());
        }
        options.add(VisionOptionDTO.builder()
                .id("typed")
                .label("Keep typed location exactly as entered")
                .value("keep typed location")
                .build());
        return options;
    }

    private String formatScheduleSummary(Map<String, String> slotData) {
        String scheduleMode = slotData.get("schedule_mode");
        if ("fixed".equals(scheduleMode)) {
            String date = formatScheduledDate(slotData.get("scheduled_date"));
            String time = formatScheduledTime(slotData.get("scheduled_time"));
            if (date != null && time != null) {
                return date + " at " + time;
            }
            if (date != null) {
                return date + ", time missing";
            }
            if (time != null) {
                return "Fixed time at " + time + ", date missing";
            }
            return "Fixed time";
        }
        if ("agreement".equals(scheduleMode)) {
            return "By agreement";
        }
        return null;
    }

    private String formatLocationSummary(Map<String, String> slotData) {
        String locationMode = slotData.get("location_mode");
        if ("profile".equals(locationMode)) {
            return "Use profile location";
        }
        if ("off".equals(locationMode)) {
            return "Hidden";
        }
        if ("custom".equals(locationMode)) {
            String locality = slotData.get("location_locality");
            String postalCode = slotData.get("location_postal_code");
            String street = slotData.get("location_street");
            String houseNumber = slotData.get("location_house_number");
            String country = slotData.get("location_country");
            if (street != null || locality != null) {
                String streetLine = joinParts(" ", street, houseNumber);
                String localityLine = joinParts(" ", postalCode, locality);
                return joinParts(", ", streetLine, localityLine, country);
            }
            return slotData.get("location_label");
        }
        return null;
    }

    private String formatScheduledDate(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return DateTimeFormatter.ofPattern("dd MMM yyyy").format(java.time.LocalDate.parse(rawValue));
        } catch (RuntimeException ignored) {
            return rawValue;
        }
    }

    private String formatScheduledTime(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return DateTimeFormatter.ofPattern("HH:mm").format(java.time.LocalTime.parse(rawValue));
        } catch (RuntimeException ignored) {
            return rawValue;
        }
    }

    private String formatScheduledAt(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "Fixed time";
        }
        try {
            return REVIEW_TIME_FORMAT.format(Instant.parse(rawValue).atZone(ZoneId.systemDefault()));
        } catch (RuntimeException ignored) {
            return rawValue;
        }
    }

    private String joinParts(String delimiter, String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(delimiter);
            }
            builder.append(part.trim());
        }
        return builder.isEmpty() ? null : builder.toString();
    }
}
