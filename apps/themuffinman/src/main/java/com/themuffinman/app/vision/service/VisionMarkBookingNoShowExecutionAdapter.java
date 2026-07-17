package com.themuffinman.app.vision.service;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
@Service public class VisionMarkBookingNoShowExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final VisionBusinessBookingMutationService service;
    public VisionMarkBookingNoShowExecutionAdapter(VisionBusinessBookingMutationService service) { this.service = service; }
    public String capabilityId() { return "mark_booking_no_show"; }
    public VisionExecutionResult execute(VisionConversation c) { try { service.noShow(Long.valueOf(c.getSlotData().get("booking_id")), c.getOwner()); return VisionExecutionResult.executedAction(capabilityId()); } catch (RuntimeException e) { return VisionExecutionResult.blocked("The booking could not be marked as no-show."); } }
}
