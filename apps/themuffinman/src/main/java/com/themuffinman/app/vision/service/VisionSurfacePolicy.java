package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.runtime.AgentSurfaceAuthority;
import com.themuffinman.app.agent.runtime.AgentSurfaceId;
import com.themuffinman.app.agent.runtime.AgentSurfacePolicy;
import com.themuffinman.app.config.VisionProperties;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class VisionSurfacePolicy implements AgentSurfacePolicy {

    private static final Set<String> EXECUTABLE_CAPABILITY_IDS = Set.of(
            "create_quest",
            "create_circle",
            "create_circle_request",
            "accept_circle_request",
            "delete_circle_request",
            "create_application",
            "update_application",
            "withdraw_application",
            "approve_application",
            "decline_application",
            "update_circle",
            "delete_circle",
            "leave_circle",
            "update_profile",
            "update_profile_location"
            ,"mark_chat_read",
            "mark_notifications_read",
            "mark_notification_read",
            "update_notification_preferences",
            "release_worker",
            "replace_worker",
            "reopen_quest",
            "cancel_quest",
            "create_thing",
            "request_borrow",
            "cancel_borrow",
            "decide_borrow",
            "return_borrow",
            "create_ride",
            "join_ride",
            "update_ride",
            "leave_ride",
            "cancel_ride",
            "start_ride",
            "complete_ride",
            "update_thing",
            "archive_thing",
            "edit_chat_message",
            "reply_to_chat_message",
            "react_to_chat_message"
            ,"create_business_profile",
            "update_business_profile"
            ,"create_gallery_image",
            "update_gallery_image",
            "delete_gallery_image"
            ,"create_availability_rule",
            "update_availability_rule",
            "delete_availability_rule",
            "create_availability_exception",
            "update_availability_exception",
            "delete_availability_exception"
            ,"confirm_booking",
            "cancel_booking"
            ,"reject_booking",
            "complete_booking",
            "mark_booking_no_show"
            ,"archive_offering"
            ,"update_quest"
            ,"create_offering",
            "update_offering"
            ,"create_booking",
            "reschedule_booking"
            ,"pause_quest"
            ,"resume_quest"
    );

    private final VisionProperties visionProperties;

    public VisionSurfacePolicy(VisionProperties visionProperties) {
        this.visionProperties = visionProperties;
    }

    @Override
    public AgentSurfaceId surfaceId() {
        return AgentSurfaceId.VISION;
    }

    @Override
    public AgentSurfaceAuthority authority() {
        return AgentSurfaceAuthority.USER_SCOPED;
    }

    @Override
    public boolean canExecuteCapability(String capabilityId) {
        return visionProperties.isExecutionEnabled() && supportedExecutionCapabilityIds().contains(capabilityId);
    }

    @Override
    public boolean allowsCrossUserTargeting() {
        return false;
    }

    @Override
    public boolean requiresExplicitConfirmation(String capabilityId) {
        return supportedExecutionCapabilityIds().contains(capabilityId);
    }

    public Set<String> supportedExecutionCapabilityIds() {
        return EXECUTABLE_CAPABILITY_IDS;
    }
}
