package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessQuoteRequestDTO;
import com.themuffinman.app.business.service.BusinessQuoteService;
import com.themuffinman.app.vision.model.VisionConversation;
public class VisionBusinessQuoteExecutionAdapter implements VisionCapabilityExecutionAdapter {
    // Quote preparation remains a reviewed Vision action; calculation belongs to BusinessQuoteService.
    private final BusinessQuoteService service;
    public VisionBusinessQuoteExecutionAdapter(BusinessQuoteService service) { this.service = service; }
    public String capabilityId() { return "quote_business_service"; }
    public VisionExecutionResult execute(VisionConversation conversation) {
        try {
            BusinessQuoteRequestDTO request = new BusinessQuoteRequestDTO();
            request.setBusinessOfferingId(Long.valueOf(conversation.getSlotData().get("business_offering_id")));
            request.setQuantity(conversation.getSlotData().get("booking_quantity") == null ? null : new java.math.BigDecimal(conversation.getSlotData().get("booking_quantity")));
            request.setDurationMinutes(conversation.getSlotData().get("booking_duration_minutes") == null ? null : Integer.valueOf(conversation.getSlotData().get("booking_duration_minutes")));
            request.setSchemaVersion(conversation.getSlotData().get("schema_version") == null ? null : Integer.valueOf(conversation.getSlotData().get("schema_version")));
            request.setStartsAt(conversation.getSlotData().get("booking_starts_at") == null ? null : java.time.Instant.parse(conversation.getSlotData().get("booking_starts_at")));
            String slug = conversation.getSlotData().get("business_slug");
            if (slug == null || slug.isBlank()) return VisionExecutionResult.blocked("The business slug is required before quoting.");
            service.quote(slug, request);
            return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException exception) { return VisionExecutionResult.blocked("The business quote could not be prepared."); }
    }
}
