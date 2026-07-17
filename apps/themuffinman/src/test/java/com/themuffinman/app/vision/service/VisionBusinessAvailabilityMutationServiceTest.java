package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.service.BusinessAvailabilityService;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisionBusinessAvailabilityMutationServiceTest {
    @Test
    void translatesRuleSlotsIntoBackendRuleCommand() {
        BusinessAvailabilityService backend = mock(BusinessAvailabilityService.class);
        VisionBusinessAvailabilityMutationService service = new VisionBusinessAvailabilityMutationService(backend);
        AppUser owner = new AppUser(); owner.setId(12L);
        Map<String, String> slots = new HashMap<>();
        slots.put("availability_day_of_week", "1");
        slots.put("availability_start_time", "09:00");
        slots.put("availability_end_time", "17:00");
        slots.put("availability_slot_minutes", "60");
        service.createRule(slots, owner);
        verify(backend).createMyRule(any(), eq(owner));
    }

    @Test
    void translatesExceptionSlotsAndOwnerIdIntoBackendCommand() {
        BusinessAvailabilityService backend = mock(BusinessAvailabilityService.class);
        VisionBusinessAvailabilityMutationService service = new VisionBusinessAvailabilityMutationService(backend);
        AppUser owner = new AppUser(); owner.setId(12L);
        Map<String, String> slots = Map.of(
                "availability_exception_id", "42",
                "availability_exception_type", "BLOCK",
                "availability_exception_start", "2026-08-01T10:00:00Z",
                "availability_exception_end", "2026-08-01T12:00:00Z"
        );
        service.updateException(slots, owner);
        verify(backend).updateMyException(eq(42L), any(), eq(owner));
    }
}
