package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.dto.VisionCanvasBlockDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionConversationSummaryDTO;
import com.themuffinman.app.vision.dto.VisionAttentionStateDTO;
import com.themuffinman.app.vision.dto.VisionExecutionCandidateDTO;
import com.themuffinman.app.vision.dto.VisionMemoryTrailDTO;
import com.themuffinman.app.vision.dto.VisionDeviceRoleDTO;
import com.themuffinman.app.vision.dto.VisionOptionDTO;
import com.themuffinman.app.vision.dto.VisionRuntimeContextDTO;
import com.themuffinman.app.vision.dto.VisionRuntimeCueDTO;
import com.themuffinman.app.vision.dto.VisionWorkspaceHandoffDTO;
import com.themuffinman.app.vision.dto.VisionWebActionDTO;
import com.themuffinman.app.vision.dto.VisionQuestDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionQuestReviewDTO;
import com.themuffinman.app.vision.dto.VisionSearchDiscoveryDTO;
import com.themuffinman.app.vision.dto.VisionSearchComparisonDTO;
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
    private static final String WEB_ACTION_CONTRACT_VERSION = "vision-web-action-v2";
    private static final String BUSINESS_CALENDAR_PATH = "/business/calendar";
    private static final String THINGS_BORROW_REQUESTS_PATH = "/things/requests";
    private static final String CIRCLES_PATH = "/circles";
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
            VisionQuestDiscoveryDTO questDiscovery,
            VisionSearchDiscoveryDTO searchDiscovery,
            VisionCapabilityPreviewDTO capabilityPreview,
            VisionMemoryTrailDTO memoryTrail
    ) {
        return assemble(conversation, turn, recentConversations, executionCandidate, questDiscovery, searchDiscovery,
                capabilityPreview, memoryTrail, null);
    }

    public VisionConversationTurnResponseDTO assemble(
            VisionConversation conversation,
            VisionTurn turn,
            List<VisionConversationSummaryDTO> recentConversations,
            VisionExecutionCandidateDTO executionCandidate,
            VisionQuestDiscoveryDTO questDiscovery,
            VisionSearchDiscoveryDTO searchDiscovery,
            VisionCapabilityPreviewDTO capabilityPreview,
        VisionMemoryTrailDTO memoryTrail,
        VisionWorkspaceHandoffDTO workspaceHandoff
    ) {
        return assemble(conversation, turn, recentConversations, executionCandidate, questDiscovery, searchDiscovery,
                capabilityPreview, memoryTrail, workspaceHandoff, null);
    }

    public VisionConversationTurnResponseDTO assemble(
            VisionConversation conversation,
            VisionTurn turn,
            List<VisionConversationSummaryDTO> recentConversations,
            VisionExecutionCandidateDTO executionCandidate,
            VisionQuestDiscoveryDTO questDiscovery,
            VisionSearchDiscoveryDTO searchDiscovery,
            VisionCapabilityPreviewDTO capabilityPreview,
            VisionMemoryTrailDTO memoryTrail,
            VisionWorkspaceHandoffDTO workspaceHandoff,
            VisionSearchComparisonDTO searchComparison
    ) {
        return VisionConversationTurnResponseDTO.builder()
                .conversationId(conversation.getId())
                .turnId(turn.getId())
                .intent(conversation.getIntent().name())
                .agentState(turn.getAgentState().name())
                .canvasMode(VisionSurfaceModeSupport.canvasModeFor(turn.getNextAction()))
                .nextAction(turn.getNextAction().name())
                .workflowState(workflowState(conversation, turn))
                .allowedActions(allowedActions(conversation, turn))
                .message(turn.getAssistantMessage())
                .requestedSlot(turn.getRequestedSlot())
                .normalizedPrompt(turn.getNormalizedPrompt())
                .translationApplied(turn.isTranslationApplied())
                .translationReliable(turn.isTranslationReliable())
                .executionEnabled(visionProperties.isExecutionEnabled())
                .runtimeContext(runtimeContext(conversation, turn))
                .workspaceHandoff(workspaceHandoff)
                .webAction(webAction(conversation, workspaceHandoff))
                .executionCandidate(executionCandidate)
                .questDiscovery(questDiscovery)
                .searchDiscovery(searchDiscovery)
                .searchComparison(searchComparison)
                .memoryTrail(memoryTrail)
                .blocks(toBlocks(conversation, turn, questDiscovery, searchDiscovery, searchComparison, capabilityPreview))
                .appliedSlotSummaries(toAppliedSlotSummaries(conversation.getSlotData(), turn.getAppliedSlotIds()))
                .slotSummaries(toSlotSummaries(conversation.getSlotData()))
                .review(toReview(conversation.getSlotData(), turn))
                .recentConversations(recentConversations)
                .build();
            }

    private VisionWebActionDTO webAction(VisionConversation conversation, VisionWorkspaceHandoffDTO workspaceHandoff) {
        if (conversation == null || conversation.getIntent() == null) {
            return null;
        }

        String routeKey;
        String canonicalPath;
        String entityFamily;
        String label;
        String targetId = null;
        String action = "NAVIGATE_TO_SURFACE";
        switch (conversation.getIntent()) {
            case VIEW_MY_WORK -> {
                routeKey = "work.my_quests";
                canonicalPath = "/work/quests";
                entityFamily = "quest";
                label = "My Work";
            }
            case VIEW_APPLICATIONS -> {
                routeKey = "work.applications";
                canonicalPath = "/work/applications";
                entityFamily = "application";
                label = "Applications";
            }
            case VIEW_CIRCLES -> {
                routeKey = "circles.index";
                canonicalPath = CIRCLES_PATH;
                entityFamily = "circle";
                label = "Circles";
            }
            case VIEW_PROFILE -> {
                routeKey = "profile.index";
                canonicalPath = "/profile";
                entityFamily = "profile";
                label = "Profile";
            }
            case VIEW_SETTINGS -> {
                routeKey = "profile.settings";
                canonicalPath = "/profile/settings";
                entityFamily = "settings";
                label = "Profile settings";
            }
            case VIEW_RIDES -> {
                String rideId = conversation.getSlotData().get("ride_id");
                if (rideId != null && !rideId.isBlank()) {
                    action = "OPEN_ENTITY_DETAIL";
                    routeKey = "rides.detail";
                    canonicalPath = "/rides/" + rideId;
                    entityFamily = "ride";
                    label = "Ride detail";
                    targetId = rideId;
                } else {
                    routeKey = "rides.list";
                    canonicalPath = "/rides";
                    entityFamily = "ride";
                    label = "Rides";
                }
            }
            case VIEW_THINGS -> {
                routeKey = "things.index";
                canonicalPath = "/things";
                entityFamily = "thing";
                label = "Things";
            }
            case VIEW_BORROW_REQUESTS -> {
                routeKey = "things.borrow";
                canonicalPath = THINGS_BORROW_REQUESTS_PATH;
                entityFamily = "borrow_request";
                label = "Borrow requests";
            }
            case VIEW_BUSINESS_BOOKINGS -> {
                routeKey = "business.bookings";
                canonicalPath = "customer".equalsIgnoreCase(conversation.getSlotData().get("booking_scope"))
                        ? "/business/my-bookings" : "/business/bookings";
                if (canonicalPath.endsWith("my-bookings")) {
                    routeKey = "business.my_bookings";
                }
                entityFamily = "business";
                label = canonicalPath.endsWith("my-bookings") ? "My appointments" : "Business bookings";
            }
            case VIEW_BUSINESS -> {
                String businessSlug = conversation.getSlotData().get("resolved_business_slug");
                if (businessSlug != null && !businessSlug.isBlank()) {
                    action = "OPEN_ENTITY_DETAIL";
                    routeKey = "business.public_profile";
                    canonicalPath = "/business/public/" + businessSlug;
                    label = conversation.getSlotData().getOrDefault("resolved_business_name", "Business profile");
                } else if ("owner".equalsIgnoreCase(conversation.getSlotData().get("business_scope"))) {
                    routeKey = "business.owner_profile";
                    canonicalPath = "/business/profile";
                    label = "My business profile";
                } else {
                    routeKey = "business.discovery";
                    canonicalPath = "/business/find";
                    label = "Business discovery";
                }
                entityFamily = "business";
            }
            case VIEW_BUSINESS_AVAILABILITY -> {
                routeKey = "business.calendar";
                canonicalPath = BUSINESS_CALENDAR_PATH;
                entityFamily = "business";
                label = "Business calendar";
            }
            case VIEW_CHAT_WORKSPACE, OPEN_CHAT, SYNC_CHAT -> {
                String conversationId = conversation.getSlotData().getOrDefault(
                        "resolved_conversation_id", conversation.getSlotData().get("conversation_id"));
                if (conversationId != null && !conversationId.isBlank()) {
                    action = "OPEN_CONVERSATION";
                    routeKey = "chat.conversation";
                    canonicalPath = "/chat/" + conversationId;
                    label = conversation.getSlotData().getOrDefault("resolved_conversation_label", "Conversation");
                } else {
                    routeKey = "chat.workspace";
                    canonicalPath = "/chat";
                    label = "Chat";
                }
                entityFamily = "chat";
            }
            case VIEW_NOTIFICATIONS -> {
                routeKey = "notifications.index";
                canonicalPath = "/notifications";
                entityFamily = "notification";
                label = "true".equalsIgnoreCase(conversation.getSlotData().get("unread_only"))
                        ? "Unread notifications" : "Notifications";
            }
            case VIEW_ACTIVITY, VIEW_QUEST_NEWS -> {
                routeKey = "activity.index";
                canonicalPath = "/activity";
                entityFamily = "activity";
                label = com.themuffinman.app.vision.model.VisionIntent.VIEW_QUEST_NEWS.equals(conversation.getIntent()) ? "Quest news" : "Activity";
            }
            case VIEW_QUEST_DETAIL -> {
                action = "OPEN_ENTITY_DETAIL";
                routeKey = "work.quest_detail";
                entityFamily = "quest";
                label = conversation.getSlotData().getOrDefault("resolved_quest_title", "Work detail");
                targetId = conversation.getSlotData().get("resolved_quest_id");
                if (targetId == null || targetId.isBlank()) {
                    return null;
                }
                canonicalPath = "/work/quests/" + targetId;
            }
            case VIEW_USER_PROFILE -> {
                action = "OPEN_ENTITY_DETAIL";
                routeKey = "people.profile";
                entityFamily = "user";
                label = conversation.getSlotData().getOrDefault("resolved_user_username", "People profile");
                targetId = conversation.getSlotData().get("resolved_user_id");
                if (targetId == null || targetId.isBlank()) {
                    return null;
                }
                canonicalPath = "/people/" + targetId;
            }
            case VIEW_THING_DETAIL -> {
                action = "OPEN_ENTITY_DETAIL";
                routeKey = "things.detail";
                entityFamily = "thing";
                label = conversation.getSlotData().getOrDefault("resolved_thing_title", "Thing detail");
                targetId = conversation.getSlotData().get("thing_listing_id");
                if (targetId == null || targetId.isBlank()) {
                    return null;
                }
                canonicalPath = "/things/" + targetId;
            }
            case VIEW_APPLICATION_DETAIL -> {
                action = "OPEN_ENTITY_DETAIL";
                routeKey = "work.application_detail";
                entityFamily = "application";
                label = conversation.getSlotData().getOrDefault("resolved_application_title", "Application detail");
                targetId = conversation.getSlotData().get("resolved_application_id");
                if (targetId == null || targetId.isBlank()) {
                    return null;
                }
                canonicalPath = "/work/applications/" + targetId;
            }
            default -> {
                return null;
            }
        }

        return VisionWebActionDTO.builder()
                .contractVersion(WEB_ACTION_CONTRACT_VERSION)
                .action(action)
                .routeKey(routeKey)
                .canonicalPath(canonicalPath)
                .entityFamily(entityFamily)
                .targetId(parseLong(targetId))
                .preview(false)
                .focus(null)
                .filters(Map.of())
                .comparisonTargetIds(List.of())
                .viewerSafeLabel(label)
                .returnContext(workspaceHandoff == null ? null : workspaceHandoff.getReturnTo())
                .allowedActions(List.of("OPEN"))
                .requiresConfirmation(false)
                .ambiguous(false)
                .recoveryOptions(List.of("RETRY", "OPEN_DIRECTLY"))
                .build();
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String workflowState(VisionConversation conversation, VisionTurn turn) {
        if (conversation.getStatus() == null) {
            return "ACTIVE";
        }
        return switch (conversation.getStatus()) {
            case ACTIVE -> turn.getNextAction() == com.themuffinman.app.vision.model.VisionNextAction.ASK_FOR_SLOT
                    ? "DRAFT" : "ACTIVE";
            case REVIEW_READY -> "REVIEW_READY";
            case COMPLETED -> "COMPLETED";
            case BLOCKED -> "FAILED";
        };
    }

    private List<String> allowedActions(VisionConversation conversation, VisionTurn turn) {
        if (conversation.getStatus() == null) {
            return List.of("CANCEL");
        }
        return switch (conversation.getStatus()) {
            case ACTIVE -> List.of("PROVIDE_INPUT", "CANCEL");
            case REVIEW_READY -> List.of("CONFIRM", "EDIT", "CANCEL");
            case COMPLETED -> List.of("OPEN_RESULT");
            case BLOCKED -> List.of("RETRY", "CANCEL");
        };
    }

    private List<VisionCanvasBlockDTO> toBlocks(
            VisionConversation conversation,
            VisionTurn turn,
            VisionQuestDiscoveryDTO questDiscovery,
            VisionSearchDiscoveryDTO searchDiscovery,
            VisionSearchComparisonDTO searchComparison,
            VisionCapabilityPreviewDTO capabilityPreview
    ) {
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

        if (searchDiscovery != null) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("search_discovery")
                    .title("Search results")
                    .body(searchDiscovery.getSummary())
                    .searchDiscovery(searchDiscovery)
                    .tone("info")
                    .build());
        }

        if (searchComparison != null) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("search_comparison")
                    .title("Comparison")
                    .body(searchComparison.getFallbackMessage() == null
                            ? "Compared permitted results."
                            : searchComparison.getFallbackMessage())
                    .searchComparison(searchComparison)
                    .tone("info")
                    .build());
        }

        if (capabilityPreview != null) {
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("result_summary")
                    .title(capabilityPreview.getTitle())
                    .body(capabilityPreview.getSummary())
                    .items(capabilityPreview.getItems())
                    .tone(capabilityPreview.getTone())
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
            if ("created_circle".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Circle created")
                        .body("Created circle " + conversation.getSlotData().get("circle_name") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("created_circle_request".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Circle request sent")
                        .body("Sent a circle request to " + conversation.getSlotData().get("circle_request_target_username") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("accepted_circle_request".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Circle request accepted")
                        .body("Accepted the circle request from " + conversation.getSlotData().get("circle_request_target_username") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("deleted_circle_request".equals(conversation.getSlotData().get("conversation_outcome"))) {
                String direction = conversation.getSlotData().get("circle_request_direction");
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("incoming".equals(direction) ? "Circle request declined" : "Circle invite cancelled")
                        .body(("incoming".equals(direction) ? "Removed the incoming request from " : "Cancelled the invite to ")
                                + conversation.getSlotData().get("circle_request_target_username") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("updated_circle".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Circle updated")
                        .body("Saved the new name for " + conversation.getSlotData().get("resolved_circle_name") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("deleted_circle".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Circle deleted")
                        .body("Deleted circle " + conversation.getSlotData().get("resolved_circle_name") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("created_application".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Application sent")
                        .body("Sent your application for " + conversation.getSlotData().get("application_quest_title") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("updated_application".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Application updated")
                        .body("Saved the latest application changes for " + conversation.getSlotData().get("application_quest_title") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("withdrawn_application".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Application withdrawn")
                        .body("Withdrew your pending application for " + conversation.getSlotData().get("application_quest_title") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("approved_application".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Application approved")
                        .body("Approved " + conversation.getSlotData().get("managed_application_applicant_username")
                                + " for " + conversation.getSlotData().get("managed_application_quest_title") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("declined_application".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Application declined")
                        .body("Declined " + conversation.getSlotData().get("managed_application_applicant_username")
                                + " for " + conversation.getSlotData().get("managed_application_quest_title") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("updated_profile".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Profile updated")
                        .body("Saved the latest profile changes for " + conversation.getSlotData().get("profile_username") + ".")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("updated_profile_location".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("success")
                        .title("Profile location updated")
                        .body("Saved the latest profile location settings.")
                        .tone("success")
                        .build());
                return blocks;
            }
            if ("cancelled".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("warning")
                        .title("Task cancelled")
                        .body("The current vision task was cancelled before execution.")
                        .tone("info")
                        .build());
                return blocks;
            }
            if ("superseded".equals(conversation.getSlotData().get("conversation_outcome"))) {
                blocks.add(VisionCanvasBlockDTO.builder()
                        .type("warning")
                        .title("Task superseded")
                        .body("A newer vision task replaced this draft before execution.")
                        .tone("info")
                        .build());
                return blocks;
            }
            String createdQuestId = conversation.getSlotData().get("created_quest_id");
            String createdQuestTitle = conversation.getSlotData().get("quest_title");
            boolean rideAction = conversation.getIntent() != null
                    && conversation.getIntent().name().endsWith("_RIDE");
            blocks.add(VisionCanvasBlockDTO.builder()
                    .type("success")
                    .title(rideAction ? "Ride action completed" : "Quest created")
                    .body(rideAction
                            ? "The ride action was completed successfully."
                            : (createdQuestId == null
                                ? "The quest was created successfully."
                                : "Created quest #" + createdQuestId + " for " + createdQuestTitle + "."))
                    .tone("success")
                    .build());
        }

        return blocks;
    }

    private VisionRuntimeContextDTO runtimeContext(VisionConversation conversation, VisionTurn turn) {
        return VisionRuntimeContextDTO.builder()
                .contractVersion("vision-shell-v1")
                .correlationId("vision-turn:" + turn.getId())
                .targetModule("vision")
                .targetRoute("/vision")
                .resourceId(conversation.getId() == null ? null : conversation.getId().toString())
                .redactions(List.of("private_profile_fields", "exact_location_without_viewer_consent"))
                .inputType(turn.getSource() == null ? "text" : turn.getSource().name().toLowerCase())
                .deviceRole(deviceRoleFor(conversation))
                .attentionState(attentionStateFor(turn))
                .sessionAnchor(sessionAnchorFor(conversation, turn))
                .actionHints(actionHintsFor(conversation, turn))
                .audioCue(runtimeCueFor(turn, "audio"))
                .hapticCue(runtimeCueFor(turn, "haptic"))
                .consentRequired(consentRequiredFor(conversation))
                .consentReason(consentReasonFor(conversation))
                .resumeAvailable(resumeAvailableFor(conversation))
                .resumeHint(resumeHintFor(conversation, turn))
                .watchFriendly(true)
                .presentationArchetype(presentationArchetype(turn))
                .density(densityFor(deviceRoleFor(conversation)))
                .primaryActionLabel(primaryActionLabel(turn))
                .visibleFields(visibleFieldsFor(turn))
                .providerStatus(conversation.getSlotData().getOrDefault("understanding_status", "unknown"))
                .providerOutcome(conversation.getSlotData().getOrDefault("understanding_outcome", "unknown"))
                .retryable(Boolean.parseBoolean(conversation.getSlotData().getOrDefault("understanding_retryable", "false")))
                .build();
    }

    private String presentationArchetype(VisionTurn turn) {
        if (turn == null || turn.getNextAction() == null) {
            return "command";
        }
        return switch (turn.getNextAction()) {
            case SHOW_RESULTS -> "focus-list";
            case SHOW_REVIEW -> "review";
            case COMPLETE -> "result";
            case BLOCKED -> "blocked";
            case ASK_FOR_SLOT -> "command";
        };
    }

    private String densityFor(VisionDeviceRoleDTO deviceRole) {
        return switch (deviceRole) {
            case WATCH -> "glance";
            case MOBILE -> "scan";
            case DESKTOP -> "inspect";
        };
    }

    private String primaryActionLabel(VisionTurn turn) {
        if (turn == null || turn.getNextAction() == null) {
            return null;
        }
        return switch (turn.getNextAction()) {
            case ASK_FOR_SLOT -> "Continue";
            case SHOW_RESULTS -> "Choose result";
            case SHOW_REVIEW -> "Review changes";
            case COMPLETE -> "Done";
            case BLOCKED -> "Resolve issue";
        };
    }

    private List<String> visibleFieldsFor(VisionTurn turn) {
        if (turn == null || turn.getRequestedSlot() == null || turn.getRequestedSlot().isBlank()) {
            return List.of("title", "status", "next_action");
        }
        return List.of("title", turn.getRequestedSlot(), "next_action");
    }

    private VisionDeviceRoleDTO deviceRoleFor(VisionConversation conversation) {
        String deviceRole = conversation.getSlotData() == null ? null : conversation.getSlotData().get("client_device_role");
        if (deviceRole == null || deviceRole.isBlank()) {
            return VisionDeviceRoleDTO.DESKTOP;
        }
        return switch (deviceRole.trim().toLowerCase()) {
            case "mobile" -> VisionDeviceRoleDTO.MOBILE;
            case "watch" -> VisionDeviceRoleDTO.WATCH;
            default -> VisionDeviceRoleDTO.DESKTOP;
        };
    }

    private VisionAttentionStateDTO attentionStateFor(VisionTurn turn) {
        if (turn == null || turn.getAgentState() == null) {
            return VisionAttentionStateDTO.PASSIVE;
        }
        return switch (turn.getAgentState()) {
            case ASKING -> VisionAttentionStateDTO.FOCUSED;
            case RECOMMENDING -> VisionAttentionStateDTO.COORDINATING;
            case REVIEW_READY -> VisionAttentionStateDTO.REVIEWING;
            case COMPLETE -> VisionAttentionStateDTO.PASSIVE;
            case BLOCKED -> VisionAttentionStateDTO.BLOCKED;
        };
    }

    private String sessionAnchorFor(VisionConversation conversation, VisionTurn turn) {
        if (conversation == null) {
            return "vision:conversation:unknown";
        }
        if (turn == null || turn.getId() == null) {
            return "vision:conversation:" + conversation.getId();
        }
        return "vision:conversation:" + conversation.getId() + ":turn:" + turn.getId();
    }

    private List<String> actionHintsFor(VisionConversation conversation, VisionTurn turn) {
        if (turn == null) {
            return List.of();
        }

        return switch (turn.getNextAction()) {
            case ASK_FOR_SLOT -> List.of(
                    "Fill " + runtimeSlotLabel(turn.getRequestedSlot()),
                    "Open Vision from this surface"
            );
            case SHOW_RESULTS -> List.of(
                    "Review the results",
                    "Continue with Vision"
            );
            case SHOW_REVIEW -> List.of(
                    "Confirm the review",
                    "Edit the current field"
            );
            case COMPLETE -> List.of(
                    "Return to the entry surface",
                    "Start another guided turn"
            );
            case BLOCKED -> List.of(
                    "Read the blocking reason",
                    "Return to the previous surface"
            );
        };
    }

    private String runtimeSlotLabel(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return "the current field";
        }
        return labelForSlot(slotId);
    }

    private VisionRuntimeCueDTO runtimeCueFor(VisionTurn turn, String channel) {
        if (turn == null) {
            return null;
        }

        String type = switch (turn.getNextAction()) {
            case ASK_FOR_SLOT -> "prompt";
            case SHOW_RESULTS -> "summary";
            case SHOW_REVIEW -> "review";
            case COMPLETE -> "complete";
            case BLOCKED -> "blocked";
        };
        String message = turn.getAssistantMessage();
        if (message == null || message.isBlank()) {
            message = turn.getNormalizedPrompt();
        }
        if (message == null || message.isBlank()) {
            message = "Vision turn updated.";
        }
        if ("haptic".equals(channel) && turn.getNextAction() == com.themuffinman.app.vision.model.VisionNextAction.COMPLETE) {
            message = "Complete.";
        }
        return VisionRuntimeCueDTO.builder()
                .type(type)
                .message(message)
                .build();
    }

    private boolean consentRequiredFor(VisionConversation conversation) {
        if (conversation == null || conversation.getIntent() == null) {
            return false;
        }

        return switch (conversation.getIntent()) {
            case OPEN_CHAT, SEND_MESSAGE,
                 CREATE_CIRCLE_REQUEST,
                 ACCEPT_CIRCLE_REQUEST,
                 DELETE_CIRCLE_REQUEST,
                 CREATE_APPLICATION,
                 UPDATE_APPLICATION,
                 WITHDRAW_APPLICATION,
                 APPROVE_APPLICATION,
                 DECLINE_APPLICATION,
                 VIEW_USER_PROFILE -> true;
            default -> false;
        };
    }

    private String consentReasonFor(VisionConversation conversation) {
        if (!consentRequiredFor(conversation) || conversation == null || conversation.getIntent() == null) {
            return null;
        }

        return switch (conversation.getIntent()) {
            case OPEN_CHAT -> "This turn can contact another person.";
            case SEND_MESSAGE -> "This turn sends a direct message after confirmation.";
            case CREATE_CIRCLE_REQUEST, ACCEPT_CIRCLE_REQUEST, DELETE_CIRCLE_REQUEST -> "This turn affects a shared circle request.";
            case CREATE_APPLICATION, UPDATE_APPLICATION, WITHDRAW_APPLICATION -> "This turn affects a shared application record.";
            case APPROVE_APPLICATION, DECLINE_APPLICATION -> "This turn changes another person's application status.";
            case VIEW_USER_PROFILE -> "This turn opens another person's profile.";
            default -> null;
        };
    }

    private boolean resumeAvailableFor(VisionConversation conversation) {
        return conversation != null && conversation.getStatus() != null && conversation.getStatus() != com.themuffinman.app.vision.model.VisionConversationStatus.COMPLETED;
    }

    private String resumeHintFor(VisionConversation conversation, VisionTurn turn) {
        if (!resumeAvailableFor(conversation)) {
            return "Start a new Vision turn.";
        }
        if (turn != null && turn.getNextAction() == com.themuffinman.app.vision.model.VisionNextAction.SHOW_REVIEW) {
            return "Resume the review for this turn.";
        }
        String requestedSlot = conversation == null || conversation.getRequestedSlot() == null
                ? (turn == null ? null : turn.getRequestedSlot())
                : conversation.getRequestedSlot();
        if (requestedSlot == null) {
            return "Resume the current Vision conversation.";
        }
        String slotLabel = runtimeSlotLabel(requestedSlot);
        return "Resume by filling " + slotLabel + ".";
    }

    private List<VisionSlotSummaryDTO> toSlotSummaries(Map<String, String> slotData) {
        List<VisionSlotSummaryDTO> summaries = new ArrayList<>();
        addSummary(summaries, "circle_name", "Circle name", slotData.get("circle_name"));
        addSummary(summaries, "target_circle_query", "Circle", slotData.get("resolved_circle_name"));
        addSummary(summaries, "target_application_query", "Application", firstNonBlank(slotData.get("application_quest_title"), slotData.get("target_application_query")));
        addSummary(summaries, "target_user", "Person", firstNonBlank(
                slotData.get("managed_application_applicant_username"),
                slotData.get("circle_request_target_username"),
                slotData.get("opened_chat_username"),
                slotData.get("resolved_profile_username"),
                slotData.get("target_user")
        ));
        addSummary(summaries, "circle_request_direction", "Direction", slotData.get("circle_request_direction"));
        addSummary(summaries, "target_quest_query", "Quest", firstNonBlank(
                slotData.get("application_quest_title"),
                slotData.get("resolved_quest_title"),
                slotData.get("target_quest_query")
        ));
        addSummary(summaries, "managed_application_quest_title", "Quest", slotData.get("managed_application_quest_title"));
        addSummary(summaries, "application_existing_message", "Current message", slotData.get("application_existing_message"));
        addSummary(summaries, "application_message", "Application message", slotData.get("application_message"));
        addSummary(summaries, "application_existing_proposed_price", "Current price", slotData.get("application_existing_proposed_price"));
        addSummary(summaries, "application_proposed_price", "Proposed price", slotData.get("application_proposed_price"));
        addSummary(summaries, "profile_username", "Username", slotData.get("profile_username"));
        addSummary(summaries, "profile_description", "Profile description", slotData.get("profile_description"));
        addSummary(summaries, "profile_location_mode", "Location mode", slotData.get("profile_location_mode"));
        addSummary(summaries, "profile_location_label", "Location", slotData.get("profile_location_label"));
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
        if (slotData.get("quest_title") == null
                && slotData.get("quest_description") == null
                && slotData.get("reward_amount") == null
                && slotData.get("visibility") == null
                && slotData.get("schedule_mode") == null
                && slotData.get("location_mode") == null) {
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
            case "target_quest_query" -> firstNonBlank(
                    slotData.get("application_quest_title"),
                    slotData.get("resolved_quest_title"),
                    slotData.get("target_quest_query")
            );
            case "target_circle_query" -> slotData.get("resolved_circle_name");
            case "target_application_query" -> firstNonBlank(slotData.get("application_quest_title"), slotData.get("target_application_query"));
            case "target_user" -> firstNonBlank(
                    slotData.get("managed_application_applicant_username"),
                    slotData.get("circle_request_target_username"),
                    slotData.get("opened_chat_username"),
                    slotData.get("resolved_profile_username"),
                    slotData.get("target_user")
            );
            case "circle_request_direction" -> {
                if ("incoming".equals(slotData.get("circle_request_direction"))) {
                    yield "Incoming request";
                }
                if ("outgoing".equals(slotData.get("circle_request_direction"))) {
                    yield "Outgoing invite";
                }
                yield slotData.get("circle_request_direction");
            }
            default -> slotData.get(slotId);
        };
    }

    private String labelForSlot(String slotId) {
        return switch (slotId) {
            case "circle_name" -> "Circle name";
            case "target_circle_query" -> "Circle";
            case "target_application_query" -> "Application";
            case "target_quest_query" -> "Quest";
            case "target_user" -> "Person";
            case "circle_request_direction" -> "Direction";
            case "application_existing_message" -> "Current message";
            case "application_message" -> "Application message";
            case "application_existing_proposed_price" -> "Current price";
            case "application_proposed_price" -> "Proposed price";
            case "profile_username" -> "Username";
            case "profile_description" -> "Profile description";
            case "profile_location_mode" -> "Location mode";
            case "profile_location_label" -> "Location";
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
            case "application_message" -> "long_text";
            case "profile_description" -> "long_text";
            case "quest_description" -> "long_text";
            case "reward_amount" -> "money";
            case "application_proposed_price" -> "money";
            case "visibility" -> "single_choice";
            case "schedule_mode", "location_mode", "profile_location_mode" -> "single_choice";
            case "location_candidate_confirmation" -> "single_choice";
            case "scheduled_date" -> "date";
            case "scheduled_time" -> "time";
            default -> "short_text";
        };
    }

    private String placeholderForSlot(String slotId) {
        return switch (slotId) {
            case "circle_name" -> "Name the circle in a few words";
            case "target_circle_query" -> "Say the exact circle name or circle id";
            case "target_application_query" -> "Say the exact application id or exact quest title";
            case "target_quest_query" -> "Say the exact quest title or quest id";
            case "target_user" -> "Say the exact username, email, or name fragment";
            case "application_message" -> "Write the message you want to send with the application";
            case "application_proposed_price" -> "Example: 20 or 20.50";
            case "profile_username" -> "Choose the username to show on your profile";
            case "profile_description" -> "Write a short profile description or bio";
            case "profile_location_mode" -> "Choose off, approximate, or exact";
            case "profile_location_label" -> "Example: Zurich, Switzerland";
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
            case "profile_location_mode" -> List.of(
                    VisionOptionDTO.builder().id("off").label("Off").value("OFF").build(),
                    VisionOptionDTO.builder().id("approximate").label("Approximate").value("APPROXIMATE").build(),
                    VisionOptionDTO.builder().id("exact").label("Exact").value("EXACT").build()
            );
            case "location_candidate_confirmation" -> locationCandidateOptions(slotData);
            case "target_user" -> chatTargetOptions(slotData);
            default -> List.of();
        };
    }

    private List<VisionOptionDTO> chatTargetOptions(Map<String, String> slotData) {
        List<VisionOptionDTO> options = new ArrayList<>();
        int count = 0;
        try {
            count = Integer.parseInt(slotData.getOrDefault("pending_chat_candidate_count", "0"));
        } catch (NumberFormatException ignored) {
            count = 0;
        }
        for (int index = 1; index <= count; index++) {
            String label = slotData.get("pending_chat_candidate_" + index + "_label");
            String value = slotData.get("pending_chat_candidate_" + index + "_value");
            if (label == null || label.isBlank() || value == null || value.isBlank()) {
                continue;
            }
            options.add(VisionOptionDTO.builder()
                    .id("chat_candidate_" + index)
                    .label("Candidate " + index + ": " + label)
                    .value(Integer.toString(index))
                    .build());
        }
        return options;
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
