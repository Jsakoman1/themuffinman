package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;

final class VisionConversationSnapshotSupport {

    private VisionConversationSnapshotSupport() {
    }

    static VisionCapabilityPreviewDTO capabilityPreview(
            VisionConversation conversation,
            AppUser currentUser,
            VisionCapabilityPreviewService visionCapabilityPreviewService
    ) {
        if (conversation == null || currentUser == null || visionCapabilityPreviewService == null) {
            return null;
        }
        return switch (conversation.getIntent()) {
            case CREATE_CIRCLE -> visionCapabilityPreviewService.previewCircleDraft(conversation.getSlotData().get("circle_name"));
            case CREATE_CIRCLE_REQUEST -> visionCapabilityPreviewService.previewCreateCircleRequestDraft(
                    conversation.getSlotData().get("circle_request_target_username")
            );
            case ACCEPT_CIRCLE_REQUEST -> visionCapabilityPreviewService.previewAcceptCircleRequestDraft(
                    conversation.getSlotData().get("circle_request_target_username")
            );
            case DELETE_CIRCLE_REQUEST -> visionCapabilityPreviewService.previewDeleteCircleRequestDraft(
                    conversation.getSlotData().get("circle_request_target_username"),
                    "incoming".equals(conversation.getSlotData().get("circle_request_direction"))
            );
            case UPDATE_CIRCLE -> visionCapabilityPreviewService.previewUpdateCircleDraft(
                    conversation.getSlotData().get("resolved_circle_name"),
                    conversation.getSlotData().get("circle_name")
            );
            case DELETE_CIRCLE -> visionCapabilityPreviewService.previewDeleteCircleDraft(
                    conversation.getSlotData().get("resolved_circle_name"),
                    conversation.getSlotData().get("resolved_circle_member_count")
            );
            case CREATE_QUEST -> visionCapabilityPreviewService.previewQuestDraft(conversation.getSlotData());
            case CREATE_APPLICATION -> visionCapabilityPreviewService.previewApplicationDraft(
                    conversation.getSlotData().get("application_quest_title"),
                    conversation.getSlotData().get("application_quest_creator"),
                    conversation.getSlotData().get("application_quest_reward_label"),
                    "true".equals(conversation.getSlotData().get("application_price_required")),
                    conversation.getSlotData().get("application_message"),
                    conversation.getSlotData().get("application_proposed_price")
            );
            case UPDATE_APPLICATION -> visionCapabilityPreviewService.previewUpdateApplicationDraft(
                    conversation.getSlotData().get("application_quest_title"),
                    conversation.getSlotData().get("application_quest_creator"),
                    conversation.getSlotData().get("application_quest_reward_label"),
                    "true".equals(conversation.getSlotData().get("application_price_required")),
                    conversation.getSlotData().get("application_existing_message"),
                    conversation.getSlotData().get("application_existing_proposed_price"),
                    conversation.getSlotData().get("application_message"),
                    conversation.getSlotData().get("application_proposed_price")
            );
            case WITHDRAW_APPLICATION -> visionCapabilityPreviewService.previewWithdrawApplicationDraft(
                    conversation.getSlotData().get("application_quest_title"),
                    conversation.getSlotData().get("application_quest_creator"),
                    conversation.getSlotData().get("application_quest_reward_label"),
                    conversation.getSlotData().get("application_existing_message"),
                    conversation.getSlotData().get("application_existing_proposed_price")
            );
            case APPROVE_APPLICATION -> visionCapabilityPreviewService.previewManagedApplicationDecisionDraft(
                    "approve_application",
                    "Application approval review",
                    "Review the pending application you are about to approve.",
                    conversation.getSlotData().get("managed_application_quest_title"),
                    conversation.getSlotData().get("managed_application_applicant_username"),
                    conversation.getSlotData().get("managed_application_message"),
                    conversation.getSlotData().get("managed_application_proposed_price")
            );
            case DECLINE_APPLICATION -> visionCapabilityPreviewService.previewManagedApplicationDecisionDraft(
                    "decline_application",
                    "Application decline review",
                    "Review the pending application you are about to decline.",
                    conversation.getSlotData().get("managed_application_quest_title"),
                    conversation.getSlotData().get("managed_application_applicant_username"),
                    conversation.getSlotData().get("managed_application_message"),
                    conversation.getSlotData().get("managed_application_proposed_price")
            );
            case UPDATE_PROFILE -> visionCapabilityPreviewService.previewProfileDraft(
                    currentUser,
                    conversation.getSlotData().get("profile_username"),
                    conversation.getSlotData().get("profile_description")
            );
            case UPDATE_PROFILE_LOCATION -> visionCapabilityPreviewService.previewProfileLocationDraft(
                    currentUser,
                    conversation.getSlotData().get("profile_location_mode"),
                    conversation.getSlotData().get("profile_location_label")
            );
            case DISCOVER_QUESTS, SEARCH -> null;
            case OPEN_CHAT -> visionCapabilityPreviewService.previewChatWorkspace(currentUser);
            case VIEW_CHAT_WORKSPACE -> visionCapabilityPreviewService.previewChatWorkspace(currentUser);
            case VIEW_PROFILE -> visionCapabilityPreviewService.previewProfile(currentUser);
            case VIEW_SETTINGS -> visionCapabilityPreviewService.previewSettings(currentUser);
            case VIEW_USER_PROFILE -> hasText(conversation.getSlotData().get("resolved_profile_user_id"))
                    ? visionCapabilityPreviewService.previewUserProfile(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_profile_user_id")))
                    : null;
            case VIEW_CIRCLES -> visionCapabilityPreviewService.previewCircles(currentUser);
            case VIEW_CIRCLE_DETAIL -> hasText(conversation.getSlotData().get("resolved_circle_id"))
                    ? visionCapabilityPreviewService.previewCircleDetail(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_circle_id")))
                    : null;
            case VIEW_QUEST_DETAIL -> hasText(conversation.getSlotData().get("resolved_quest_id"))
                    ? visionCapabilityPreviewService.previewQuestDetail(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_quest_id")))
                    : null;
            case VIEW_NOTIFICATIONS -> visionCapabilityPreviewService.previewNotifications(currentUser);
            case VIEW_QUEST_NEWS -> visionCapabilityPreviewService.previewQuestNews(currentUser);
            case VIEW_APPLICATIONS -> visionCapabilityPreviewService.previewApplications(currentUser);
            case VIEW_APPLICATION_DETAIL -> hasText(conversation.getSlotData().get("application_id"))
                    ? visionCapabilityPreviewService.previewApplicationDetail(currentUser, Long.parseLong(conversation.getSlotData().get("application_id")))
                    : null;
            case VIEW_THINGS -> visionCapabilityPreviewService.previewThings(currentUser);
            case UNSUPPORTED -> null;
        };
    }

    static String readOnlySnapshotMessage(VisionIntent intent) {
        return switch (intent) {
            case VIEW_PROFILE -> "Profile.";
            case VIEW_SETTINGS -> "Settings.";
            case VIEW_USER_PROFILE -> "User profile.";
            case VIEW_CIRCLES -> "Showing your circles.";
            case VIEW_CIRCLE_DETAIL -> "Showing the selected circle.";
            case VIEW_APPLICATIONS -> "Applications.";
            case VIEW_APPLICATION_DETAIL -> "Application.";
            case VIEW_NOTIFICATIONS -> "Notifications.";
            case VIEW_QUEST_NEWS -> "Quest news.";
            case VIEW_CHAT_WORKSPACE -> "Chat.";
            case VIEW_QUEST_DETAIL -> "Quest.";
            case VIEW_THINGS -> "Things.";
            default -> "Vision snapshot.";
        };
    }

    static String resetReadOnlySnapshotMessage(VisionIntent intent) {
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, VIEW_CIRCLES, VIEW_APPLICATIONS, VIEW_CHAT_WORKSPACE, VIEW_NOTIFICATIONS, VIEW_QUEST_NEWS, VIEW_THINGS ->
                    "The current view was reset. " + readOnlySnapshotMessage(intent);
            default -> "The current view was reset.";
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
