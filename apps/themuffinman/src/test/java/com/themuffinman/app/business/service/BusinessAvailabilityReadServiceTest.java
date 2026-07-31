package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.dto.BusinessAvailabilityWindowDTO;
import com.themuffinman.app.business.repository.BusinessAvailabilityExceptionRepository;
import com.themuffinman.app.business.repository.BusinessAvailabilityRuleRepository;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessAvailabilityReadServiceTest {

    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private BusinessOfferingRepository businessOfferingRepository;
    @Mock private BusinessAvailabilityRuleRepository businessAvailabilityRuleRepository;
    @Mock private BusinessAvailabilityExceptionRepository businessAvailabilityExceptionRepository;
    @Mock private BusinessAvailabilityComputationService businessAvailabilityComputationService;
    @Mock private BusinessResourceAssignmentService businessResourceAssignmentService;
    @InjectMocks private BusinessAvailabilityReadService service;

    @Test
    void resolvesCustomerDateAtBusinessLocalDayBoundaries() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L); profile.setSlug("dog-groomer"); profile.setActive(true); profile.setBookingEnabled(true); profile.setTimezone("Europe/Zurich");
        BusinessOffering offering = new BusinessOffering();
        offering.setId(20L); offering.setBusinessProfile(profile); offering.setActive(true); offering.setTitle("Trim");
        LocalDate date = LocalDate.of(2026, 3, 29);

        when(businessProfileRepository.findBySlug("dog-groomer")).thenReturn(Optional.of(profile));
        when(businessOfferingRepository.findActiveByBusinessProfileId(10L)).thenReturn(List.of(offering));
        when(businessAvailabilityRuleRepository.findActiveByBusinessProfileId(10L)).thenReturn(List.of());
        when(businessAvailabilityExceptionRepository.findByBusinessProfileId(10L)).thenReturn(List.of());
        when(businessAvailabilityComputationService.deriveWindows(any(), any(), any(), any(), any(), any())).thenReturn(List.of());

        service.getPublicAvailabilityForBusinessDate("dog-groomer", 20L, date);

        ArgumentCaptor<Instant> from = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> to = ArgumentCaptor.forClass(Instant.class);
        verify(businessAvailabilityComputationService).deriveWindows(eq(profile), eq(offering), any(), any(), from.capture(), to.capture());
        ZoneId zone = ZoneId.of("Europe/Zurich");
        assertEquals(date.atStartOfDay(zone).toInstant(), from.getValue());
        assertEquals(date.plusDays(1).atStartOfDay(zone).toInstant().minusNanos(1), to.getValue());
    }

    @Test
    void omitsSlotsWhoseRequiredResourcesAreNotAvailable() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L); profile.setSlug("dog-groomer"); profile.setActive(true); profile.setBookingEnabled(true); profile.setTimezone("Europe/Zurich");
        BusinessOffering offering = new BusinessOffering();
        offering.setId(20L); offering.setBusinessProfile(profile); offering.setActive(true); offering.setTitle("Trim");
        Instant unavailableStart = Instant.parse("2026-08-12T09:00:00Z");
        Instant unavailableEnd = Instant.parse("2026-08-12T09:45:00Z");
        Instant availableStart = Instant.parse("2026-08-12T10:00:00Z");
        Instant availableEnd = Instant.parse("2026-08-12T10:45:00Z");

        when(businessProfileRepository.findBySlug("dog-groomer")).thenReturn(Optional.of(profile));
        when(businessOfferingRepository.findActiveByBusinessProfileId(10L)).thenReturn(List.of(offering));
        when(businessAvailabilityRuleRepository.findActiveByBusinessProfileId(10L)).thenReturn(List.of());
        when(businessAvailabilityExceptionRepository.findByBusinessProfileId(10L)).thenReturn(List.of());
        when(businessAvailabilityComputationService.deriveWindows(any(), any(), any(), any(), any(), any())).thenReturn(List.of(
                BusinessAvailabilityWindowDTO.builder().startsAt(unavailableStart).endsAt(unavailableEnd).build(),
                BusinessAvailabilityWindowDTO.builder().startsAt(availableStart).endsAt(availableEnd).build()
        ));
        when(businessResourceAssignmentService.hasAvailableResources(offering, unavailableStart, unavailableEnd)).thenReturn(false);
        when(businessResourceAssignmentService.hasAvailableResources(offering, availableStart, availableEnd)).thenReturn(true);

        var result = service.getPublicAvailabilityForBusinessDate("dog-groomer", 20L, LocalDate.of(2026, 8, 12));

        assertEquals(List.of(availableStart), result.getItems().stream().map(BusinessAvailabilityWindowDTO::getStartsAt).toList());
    }

    @Test
    void calendarIncludesUnavailableBusinessLocalDays() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L); profile.setSlug("dog-groomer"); profile.setActive(true); profile.setBookingEnabled(true); profile.setTimezone("Europe/Zurich");
        BusinessOffering offering = new BusinessOffering();
        offering.setId(20L); offering.setBusinessProfile(profile); offering.setActive(true); offering.setTitle("Trim");
        Instant start = Instant.parse("2026-08-12T09:00:00Z");
        Instant end = Instant.parse("2026-08-12T09:45:00Z");
        when(businessProfileRepository.findBySlug("dog-groomer")).thenReturn(Optional.of(profile));
        when(businessOfferingRepository.findActiveByBusinessProfileId(10L)).thenReturn(List.of(offering));
        when(businessAvailabilityRuleRepository.findActiveByBusinessProfileId(10L)).thenReturn(List.of());
        when(businessAvailabilityExceptionRepository.findByBusinessProfileId(10L)).thenReturn(List.of());
        when(businessAvailabilityComputationService.deriveWindows(any(), any(), any(), any(), any(), any())).thenReturn(List.of(BusinessAvailabilityWindowDTO.builder().startsAt(start).endsAt(end).build()));
        when(businessResourceAssignmentService.hasAvailableResources(offering, start, end)).thenReturn(true);

        var result = service.getPublicAvailabilityCalendar("dog-groomer", 20L, LocalDate.of(2026, 8, 12), 2);

        assertEquals("Europe/Zurich", result.getTimezone());
        assertEquals(List.of("LIMITED", "UNAVAILABLE"), result.getDays().stream().map(day -> day.getAvailabilityState()).toList());
        assertEquals(start, result.getDays().getFirst().getSlots().getFirst().getStartsAt());
        assertEquals(end, result.getDays().getFirst().getSlots().getFirst().getEndsAt());
        assertEquals("MONTH", result.getView());
    }
}
