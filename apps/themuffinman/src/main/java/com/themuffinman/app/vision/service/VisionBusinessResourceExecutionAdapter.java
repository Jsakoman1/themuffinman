package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.service.BusinessResourceService;
import com.themuffinman.app.vision.model.VisionConversation;
public class VisionBusinessResourceExecutionAdapter implements VisionCapabilityExecutionAdapter {
    // Resource visibility is constrained by the owner-scoped Business resource service.
    private final BusinessResourceService service;
    public VisionBusinessResourceExecutionAdapter(BusinessResourceService service) { this.service = service; }
    public String capabilityId() { return "view_business_resources"; }
    public VisionExecutionResult execute(VisionConversation conversation) {
        try { service.getOwnerResources(Long.valueOf(conversation.getSlotData().get("business_profile_id")), conversation.getOwner()); return VisionExecutionResult.executedAction(capabilityId()); }
        catch (RuntimeException exception) { return VisionExecutionResult.blocked("The business resources could not be read."); }
    }
}
