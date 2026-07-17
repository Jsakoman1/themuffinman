package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionConfirmBookingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final VisionBusinessBookingMutationService service;
    public VisionConfirmBookingExecutionAdapter(VisionBusinessBookingMutationService service) { this.service = service; }
    @Override public String capabilityId() { return "confirm_booking"; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        try {
            service.confirm(Long.valueOf(conversation.getSlotData().get("booking_id")), conversation.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException exception) { return VisionExecutionResult.blocked("The booking could not be confirmed."); }
    }
}
