package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionRescheduleBookingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final VisionBusinessBookingMutationService service;
    public VisionRescheduleBookingExecutionAdapter(VisionBusinessBookingMutationService service) { this.service = service; }
    @Override public String capabilityId() { return "reschedule_booking"; }
    @Override public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) return VisionExecutionResult.blocked("Conversation owner is required.");
        String bookingId = conversation.getSlotData().get("booking_id");
        String startsAt = conversation.getSlotData().get("booking_starts_at");
        String endsAt = conversation.getSlotData().get("booking_ends_at");
        if (bookingId == null || startsAt == null || endsAt == null) return VisionExecutionResult.blocked("Booking id, start, and end are required.");
        try { service.reschedule(Long.parseLong(bookingId), startsAt, endsAt, conversation.getOwner()); return VisionExecutionResult.executedAction(capabilityId()); }
        catch (NumberFormatException exception) { return VisionExecutionResult.blocked("The booking id is invalid."); }
    }
}
