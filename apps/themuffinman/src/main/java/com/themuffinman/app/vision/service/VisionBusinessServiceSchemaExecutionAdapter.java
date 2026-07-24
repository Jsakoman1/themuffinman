package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.service.BusinessOfferingSchemaService;
import com.themuffinman.app.vision.model.VisionConversation;
public class VisionBusinessServiceSchemaExecutionAdapter implements VisionCapabilityExecutionAdapter {
    // Owner permission is evaluated by the shared schema service, never by Vision.
    private final BusinessOfferingSchemaService service;
    public VisionBusinessServiceSchemaExecutionAdapter(BusinessOfferingSchemaService service) { this.service = service; }
    public String capabilityId() { return "configure_business_service_schema"; }
    public VisionExecutionResult execute(VisionConversation conversation) {
        try { service.getOwnerSchema(Long.valueOf(conversation.getSlotData().get("business_offering_id")), conversation.getOwner()); return VisionExecutionResult.executedAction(capabilityId()); }
        catch (RuntimeException exception) { return VisionExecutionResult.blocked("The business service schema could not be read."); }
    }
}
