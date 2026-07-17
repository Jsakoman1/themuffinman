package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.service.BusinessCreateBookingUseCase;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VisionCreateBookingExecutionAdapterTest {
    @Test
    void createsCustomerBookingThroughBackendUseCase() {
        BusinessCreateBookingUseCase service = mock(BusinessCreateBookingUseCase.class);
        VisionConversation c = new VisionConversation();
        c.setOwner(new AppUser());
        c.setSlotData(new LinkedHashMap<>(Map.of("business_offering_id", "42", "booking_starts_at", "2026-08-01T10:00:00Z", "booking_ends_at", "2026-08-01T11:00:00Z")));
        when(service.createCustomerBooking(any(), eq(c.getOwner()))).thenReturn(BusinessBookingResponseDTO.builder().build());
        assertTrue(new VisionCreateBookingExecutionAdapter(service).execute(c).isExecuted());
        verify(service).createCustomerBooking(any(), eq(c.getOwner()));
    }
}
