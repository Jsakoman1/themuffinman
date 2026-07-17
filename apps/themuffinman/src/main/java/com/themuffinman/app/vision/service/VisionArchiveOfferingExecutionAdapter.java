package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.service.BusinessOfferingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;

@Service
public class VisionArchiveOfferingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final BusinessOfferingService service;
    public VisionArchiveOfferingExecutionAdapter(BusinessOfferingService service) { this.service = service; }
    public String capabilityId() { return "archive_offering"; }
    public VisionExecutionResult execute(VisionConversation conversation) {
        try {
            service.deleteMyOffering(Long.valueOf(conversation.getSlotData().get("offering_id")), conversation.getOwner());
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException exception) { return VisionExecutionResult.blocked("The offering could not be archived."); }
    }
}
