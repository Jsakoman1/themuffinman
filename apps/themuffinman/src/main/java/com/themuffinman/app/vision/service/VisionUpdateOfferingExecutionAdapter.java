package com.themuffinman.app.vision.service;
import com.themuffinman.app.business.service.BusinessOfferingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
@Service public class VisionUpdateOfferingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final BusinessOfferingService service;
    public VisionUpdateOfferingExecutionAdapter(BusinessOfferingService service) { this.service = service; }
    public String capabilityId() { return "update_offering"; }
    public VisionExecutionResult execute(VisionConversation c) { try { service.updateMyOfferingTitleForVision(Long.valueOf(c.getSlotData().get("offering_id")), c.getSlotData().get("offering_title"), c.getOwner()); return VisionExecutionResult.executedAction(capabilityId()); } catch (RuntimeException e) { return VisionExecutionResult.blocked("The offering could not be updated."); } }
}
