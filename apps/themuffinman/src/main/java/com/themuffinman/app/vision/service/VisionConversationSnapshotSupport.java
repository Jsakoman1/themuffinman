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
            case DISCOVER_QUESTS, SEARCH, CREATE_RIDE, JOIN_RIDE, UPDATE_RIDE, LEAVE_RIDE, CANCEL_RIDE, START_RIDE, COMPLETE_RIDE, VIEW_RIDES -> null;
            case OPEN_CHAT -> visionCapabilityPreviewService.previewChatWorkspace(currentUser);
            case VIEW_CHAT_WORKSPACE -> visionCapabilityPreviewService.previewChatWorkspace(currentUser);
            case SYNC_CHAT -> hasText(conversation.getSlotData().get("opened_chat_conversation_id"))
                    ? visionCapabilityPreviewService.previewChatSync(currentUser, Long.parseLong(conversation.getSlotData().get("opened_chat_conversation_id")))
                    : null;
            case VIEW_CHAT_ATTACHMENT -> hasText(conversation.getSlotData().get("chat_conversation_id"))
                    && hasText(conversation.getSlotData().get("chat_message_id"))
                    ? visionCapabilityPreviewService.previewChatAttachment(currentUser,
                    Long.parseLong(conversation.getSlotData().get("chat_conversation_id")),
                    Long.parseLong(conversation.getSlotData().get("chat_message_id")))
                    : null;
            case MARK_CHAT_READ -> null;
            case MARK_NOTIFICATIONS_READ -> null;
            case MARK_NOTIFICATION_READ -> null;
            case UPDATE_NOTIFICATION_PREFERENCES -> null;
            case RELEASE_WORKER, REPLACE_WORKER -> null;
            case REOPEN_QUEST -> null;
            case CANCEL_QUEST -> null;
            case PAUSE_QUEST, RESUME_QUEST -> null;
            case RESCHEDULE_BOOKING -> null;
            case CREATE_THING -> null;
            case REQUEST_BORROW -> null;
            case CANCEL_BORROW, DECIDE_BORROW, RETURN_BORROW, UPDATE_THING, ARCHIVE_THING, EDIT_CHAT_MESSAGE, REPLY_TO_CHAT_MESSAGE, REACT_TO_CHAT_MESSAGE, CREATE_BUSINESS_PROFILE, UPDATE_BUSINESS_PROFILE, CREATE_GALLERY_IMAGE, UPDATE_GALLERY_IMAGE, DELETE_GALLERY_IMAGE, CREATE_AVAILABILITY_RULE, UPDATE_AVAILABILITY_RULE, DELETE_AVAILABILITY_RULE, CREATE_AVAILABILITY_EXCEPTION, UPDATE_AVAILABILITY_EXCEPTION, DELETE_AVAILABILITY_EXCEPTION, LEAVE_CIRCLE, CONFIRM_BOOKING, CANCEL_BOOKING, REJECT_BOOKING, COMPLETE_BOOKING, MARK_BOOKING_NO_SHOW, ARCHIVE_OFFERING, UPDATE_QUEST, CREATE_OFFERING, UPDATE_OFFERING, CREATE_BOOKING -> null;
            case VIEW_PROFILE -> visionCapabilityPreviewService.previewProfile(currentUser);
            case VIEW_SETTINGS -> visionCapabilityPreviewService.previewSettings(currentUser);
            // Business discovery is valid before an owner profile exists. A
            // missing private dashboard must not prevent the typed Web action
            // from opening the public Business directory.
            case VIEW_BUSINESS -> safeBusinessPreview(currentUser, visionCapabilityPreviewService);
            case VIEW_BUSINESS_AVAILABILITY -> visionCapabilityPreviewService.previewBusinessAvailability(currentUser);
            case VIEW_BUSINESS_BOOKINGS -> visionCapabilityPreviewService.previewBusinessBookings(currentUser);
            case VIEW_USER_PROFILE -> hasText(conversation.getSlotData().get("resolved_profile_user_id"))
                    ? visionCapabilityPreviewService.previewUserProfile(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_profile_user_id")))
                    : null;
            case VIEW_CIRCLES -> visionCapabilityPreviewService.previewCircles(currentUser);
            case VIEW_CIRCLE_DETAIL -> hasText(conversation.getSlotData().get("resolved_circle_id"))
                    ? visionCapabilityPreviewService.previewCircleDetail(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_circle_id")))
                    : null;
            case VIEW_ACCESSIBLE_CIRCLE -> hasText(conversation.getSlotData().get("accessible_circle_id"))
                    ? visionCapabilityPreviewService.previewAccessibleCircle(currentUser, Long.parseLong(conversation.getSlotData().get("accessible_circle_id")))
                    : null;
            case VIEW_QUEST_DETAIL -> hasText(conversation.getSlotData().get("resolved_quest_id"))
                    ? visionCapabilityPreviewService.previewQuestDetail(currentUser, Long.parseLong(conversation.getSlotData().get("resolved_quest_id")))
                    : null;
            case VIEW_MY_WORK -> null;
            case VIEW_NOTIFICATIONS -> visionCapabilityPreviewService.previewNotifications(currentUser);
            case VIEW_ACTIVITY -> visionCapabilityPreviewService.previewActivity(currentUser);
            case VIEW_QUEST_NEWS -> visionCapabilityPreviewService.previewQuestNews(currentUser);
            case VIEW_APPLICATIONS -> visionCapabilityPreviewService.previewApplications(currentUser);
            case VIEW_APPLICATION_DETAIL -> hasText(conversation.getSlotData().get("application_id"))
                    ? visionCapabilityPreviewService.previewApplicationDetail(currentUser, Long.parseLong(conversation.getSlotData().get("application_id")))
                    : null;
            case VIEW_THINGS -> visionCapabilityPreviewService.previewThings(currentUser);
            case VIEW_THING_DETAIL -> hasText(conversation.getSlotData().get("thing_listing_id"))
                    ? visionCapabilityPreviewService.previewThingDetail(currentUser, Long.parseLong(conversation.getSlotData().get("thing_listing_id")))
                    : null;
            case VIEW_BORROW_REQUESTS -> visionCapabilityPreviewService.previewBorrowRequests(currentUser);
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
            case VIEW_ACCESSIBLE_CIRCLE -> "Showing the accessible circle.";
            case VIEW_APPLICATIONS -> "Applications.";
            case VIEW_APPLICATION_DETAIL -> "Application.";
            case VIEW_NOTIFICATIONS -> "Notifications.";
            case VIEW_ACTIVITY -> "Activity.";
            case VIEW_QUEST_NEWS -> "Quest news.";
            case VIEW_CHAT_WORKSPACE -> "Chat.";
            case VIEW_CHAT_ATTACHMENT -> "Chat attachment.";
            case VIEW_BUSINESS -> "Business.";
            case VIEW_BUSINESS_AVAILABILITY -> "Business availability.";
            case VIEW_BUSINESS_BOOKINGS -> "Business bookings.";
            case VIEW_QUEST_DETAIL -> "Quest.";
            case VIEW_MY_WORK -> "My Work.";
            case VIEW_THINGS -> "Things.";
            case VIEW_THING_DETAIL -> "Thing listing.";
            case VIEW_BORROW_REQUESTS -> "Borrow requests.";
            case VIEW_RIDES -> "Rides.";
            default -> "Vision snapshot.";
        };
    }

    static String resetReadOnlySnapshotMessage(VisionIntent intent) {
        return switch (intent) {
            case VIEW_PROFILE, VIEW_SETTINGS, VIEW_BUSINESS, VIEW_BUSINESS_AVAILABILITY, VIEW_BUSINESS_BOOKINGS, VIEW_CIRCLES, VIEW_APPLICATIONS, VIEW_CHAT_WORKSPACE, VIEW_NOTIFICATIONS, VIEW_ACTIVITY, VIEW_QUEST_NEWS, VIEW_THINGS, VIEW_BORROW_REQUESTS ->
                    "The current view was reset. " + readOnlySnapshotMessage(intent);
            default -> "The current view was reset.";
        };
    }

    private static VisionCapabilityPreviewDTO safeBusinessPreview(
            AppUser currentUser,
            VisionCapabilityPreviewService visionCapabilityPreviewService
    ) {
        try {
            return visionCapabilityPreviewService.previewBusiness(currentUser);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
