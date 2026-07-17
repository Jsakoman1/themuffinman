package com.themuffinman.app.vision.service;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
@Service public class VisionRejectBookingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final VisionBusinessBookingMutationService service;
    public VisionRejectBookingExecutionAdapter(VisionBusinessBookingMutationService service) { this.service = service; }
    public String capabilityId() { return "reject_booking"; }
    public VisionExecutionResult execute(VisionConversation c) { try { service.reject(Long.valueOf(c.getSlotData().get("booking_id")), c.getOwner()); return VisionExecutionResult.executedAction(capabilityId()); } catch (RuntimeException e) { return VisionExecutionResult.blocked("The booking could not be rejected."); } }
}
