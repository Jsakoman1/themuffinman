package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.dto.BusinessBookingPreviewRequestDTO;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessBookingPreviewServiceTest {

    @Mock
    private BusinessOfferingRepository businessOfferingRepository;

    @InjectMocks
    private BusinessBookingPreviewService service;

    @Test
    void derivesEndTimeFromBackendOfferingDurationAndPreservesTimezone() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(3L);
        profile.setSlug("studio");
        profile.setActive(true);
        profile.setTimezone("Europe/Zurich");

        BusinessOffering offering = new BusinessOffering();
        offering.setId(7L);
        offering.setTitle("Consultation");
        offering.setBusinessProfile(profile);
        offering.setActive(true);
        offering.setDefaultDurationMinutes(90);

        when(businessOfferingRepository.findById(7L)).thenReturn(Optional.of(offering));

        BusinessBookingPreviewRequestDTO request = new BusinessBookingPreviewRequestDTO();
        request.setBusinessOfferingId(7L);
        request.setStartsAt(Instant.parse("2026-07-20T08:00:00Z"));

        var result = service.preview("studio", request);

        assertEquals(Instant.parse("2026-07-20T09:30:00Z"), result.getEndsAt());
        assertEquals("Europe/Zurich", result.getTimezone());
        assertEquals(90, result.getDurationMinutes());
    }
}
