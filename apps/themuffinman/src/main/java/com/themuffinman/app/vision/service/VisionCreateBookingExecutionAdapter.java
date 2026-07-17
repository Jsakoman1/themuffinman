package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessBookingRequestDTO;
import com.themuffinman.app.business.service.BusinessCreateBookingUseCase;
import com.themuffinman.app.vision.model.VisionConversation;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class VisionCreateBookingExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private final BusinessCreateBookingUseCase service;
    public VisionCreateBookingExecutionAdapter(BusinessCreateBookingUseCase service) { this.service = service; }
    public String capabilityId() { return "create_booking"; }
    public VisionExecutionResult execute(VisionConversation c) {
        try {
            BusinessBookingRequestDTO dto = new BusinessBookingRequestDTO();
            dto.setBusinessOfferingId(Long.valueOf(c.getSlotData().get("business_offering_id")));
            dto.setStartsAt(Instant.parse(c.getSlotData().get("booking_starts_at")));
            dto.setEndsAt(Instant.parse(c.getSlotData().get("booking_ends_at")));
            return executed(service.createCustomerBooking(dto, c.getOwner()));
        } catch (RuntimeException e) { return VisionExecutionResult.blocked("The appointment could not be created."); }
    }
    private VisionExecutionResult executed(Object ignored) { return VisionExecutionResult.executedAction(capabilityId()); }
}
