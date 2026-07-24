package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.service.BusinessOfferingSchemaService;
import com.themuffinman.app.vision.model.VisionConversation;
public class VisionBusinessPricingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    // Vision reads pricing rules; it does not duplicate or mutate price calculations.
    private final BusinessOfferingSchemaService service;
    public VisionBusinessPricingExecutionAdapter(BusinessOfferingSchemaService service) { this.service = service; }
    public String capabilityId() { return "view_business_pricing_rules"; }
    public VisionExecutionResult execute(VisionConversation conversation) {
        try { service.getOwnerSchema(Long.valueOf(conversation.getSlotData().get("business_offering_id")), conversation.getOwner()); return VisionExecutionResult.executedAction(capabilityId()); }
        catch (RuntimeException exception) { return VisionExecutionResult.blocked("The business pricing rules could not be read."); }
    }
}
