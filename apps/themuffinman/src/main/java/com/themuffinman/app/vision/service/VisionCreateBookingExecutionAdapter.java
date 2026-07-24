package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessBookingRequestDTO;
import com.themuffinman.app.business.service.BusinessCreateBookingUseCase;
import com.themuffinman.app.vision.model.VisionConversation;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class VisionCreateBookingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    // The final booking write still goes through BusinessCreateBookingUseCase for atomic revalidation.
    private final BusinessCreateBookingUseCase service;
    public VisionCreateBookingExecutionAdapter(BusinessCreateBookingUseCase service) { this.service = service; }
    public String capabilityId() { return "create_booking"; }
    public VisionExecutionResult execute(VisionConversation c) {
        try {
            BusinessBookingRequestDTO dto = new BusinessBookingRequestDTO();
            dto.setBusinessOfferingId(Long.valueOf(c.getSlotData().get("business_offering_id")));
            dto.setStartsAt(Instant.parse(c.getSlotData().get("booking_starts_at")));
            dto.setEndsAt(Instant.parse(c.getSlotData().get("booking_ends_at")));
            if (c.getSlotData().get("booking_quantity") != null) {
                dto.setQuantity(new java.math.BigDecimal(c.getSlotData().get("booking_quantity")));
            }
            dto.setIdempotencyKey("vision-" + c.getId());
            dto.setAnswers(stringMap(c.getSlotData(), "demand_"));
            dto.setSelectedOptions(stringMap(c.getSlotData(), "option_"));
            dto.setCustomerNote("Vision-confirmed booking");
            return executed(service.createCustomerBooking(dto, c.getOwner()));
        } catch (RuntimeException e) { return VisionExecutionResult.blocked("The appointment could not be created."); }
    }
    private java.util.Map<String, String> stringMap(java.util.Map<String, String> slots, String prefix) {
        java.util.Map<String, String> result = new java.util.LinkedHashMap<>();
        slots.forEach((key, value) -> { if (key.startsWith(prefix)) result.put(key.substring(prefix.length()), value); });
        return result;
    }
    private VisionExecutionResult executed(Object ignored) { return VisionExecutionResult.executedAction(capabilityId()); }
}
