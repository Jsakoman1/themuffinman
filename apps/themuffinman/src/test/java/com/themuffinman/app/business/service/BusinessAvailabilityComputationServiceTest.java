package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessAvailabilityException;
import com.themuffinman.app.business.model.BusinessAvailabilityExceptionType;
import com.themuffinman.app.business.model.BusinessAvailabilityRule;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessAvailabilityComputationServiceTest {

    private final BusinessAvailabilityComputationService service = new BusinessAvailabilityComputationService();

    @Test
    void deriveWindowsBuildsGranularSlotsInBusinessTimezone() {
        BusinessProfile profile = profile();
        BusinessOffering offering = offering(profile);
        BusinessAvailabilityRule rule = rule(profile, offering);

        Instant from = LocalDate.of(2026, 7, 13).atTime(8, 0).atZone(ZoneId.of(profile.getTimezone())).toInstant();
        Instant to = LocalDate.of(2026, 7, 13).atTime(12, 0).atZone(ZoneId.of(profile.getTimezone())).toInstant();

        var result = service.deriveWindows(profile, offering, List.of(rule), List.of(), from, to);

        assertEquals(4, result.size());
        assertEquals("Europe/Zurich", result.getFirst().getTimezone());
        assertEquals(1, result.getFirst().getEffectiveCapacity());
    }

    @Test
    void deriveWindowsSkipsBlockedSlots() {
        BusinessProfile profile = profile();
        BusinessOffering offering = offering(profile);
        BusinessAvailabilityRule rule = rule(profile, offering);
        Instant blockStart = LocalDate.of(2026, 7, 13).atTime(9, 0).atZone(ZoneId.of(profile.getTimezone())).toInstant();
        Instant blockEnd = LocalDate.of(2026, 7, 13).atTime(10, 0).atZone(ZoneId.of(profile.getTimezone())).toInstant();

        BusinessAvailabilityException exception = new BusinessAvailabilityException();
        exception.setBusinessProfile(profile);
        exception.setBusinessOffering(offering);
        exception.setExceptionType(BusinessAvailabilityExceptionType.BLOCK);
        exception.setStartAt(blockStart);
        exception.setEndAt(blockEnd);

        Instant from = LocalDate.of(2026, 7, 13).atTime(8, 0).atZone(ZoneId.of(profile.getTimezone())).toInstant();
        Instant to = LocalDate.of(2026, 7, 13).atTime(12, 0).atZone(ZoneId.of(profile.getTimezone())).toInstant();

        var result = service.deriveWindows(profile, offering, List.of(rule), List.of(exception), from, to);

        assertEquals(3, result.size());
    }

    @Test
    void deriveWindowsKeepsSlotSequenceAcrossDstSpringForwardGap() {
        BusinessProfile profile = profile();
        BusinessOffering offering = offering(profile);
        BusinessAvailabilityRule rule = rule(profile, offering);
        rule.setDayOfWeek(7);
        rule.setStartTimeLocal(LocalTime.of(0, 0));
        rule.setEndTimeLocal(LocalTime.of(4, 0));

        Instant from = LocalDate.of(2026, 3, 29).atTime(0, 0).atZone(ZoneId.of(profile.getTimezone())).toInstant();
        Instant to = LocalDate.of(2026, 3, 29).atTime(4, 0).atZone(ZoneId.of(profile.getTimezone())).toInstant();

        var result = service.deriveWindows(profile, offering, List.of(rule), List.of(), from, to);

        assertEquals(3, result.size());
        assertEquals(LocalTime.of(0, 0), result.get(0).getStartsAt().atZone(ZoneId.of(profile.getTimezone())).toLocalTime());
        assertEquals(LocalTime.of(1, 0), result.get(1).getStartsAt().atZone(ZoneId.of(profile.getTimezone())).toLocalTime());
        assertEquals(LocalTime.of(3, 0), result.get(2).getStartsAt().atZone(ZoneId.of(profile.getTimezone())).toLocalTime());
    }

    @Test
    void deriveWindowsRequiresTimezone() {
        BusinessProfile profile = profile();
        profile.setTimezone(null);
        BusinessOffering offering = offering(profile);

        assertThrows(ResponseStatusException.class, () -> service.deriveWindows(
                profile,
                offering,
                List.of(),
                List.of(),
                Instant.parse("2026-07-13T08:00:00Z"),
                Instant.parse("2026-07-13T10:00:00Z")
        ));
    }

    private BusinessProfile profile() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(1L);
        profile.setSlug("dog-place");
        profile.setTimezone("Europe/Zurich");
        profile.setBookingEnabled(true);
        return profile;
    }

    private BusinessOffering offering(BusinessProfile profile) {
        BusinessOffering offering = new BusinessOffering();
        offering.setId(2L);
        offering.setBusinessProfile(profile);
        offering.setTitle("Full Grooming");
        offering.setSlotCapacity(1);
        return offering;
    }

    private BusinessAvailabilityRule rule(BusinessProfile profile, BusinessOffering offering) {
        BusinessAvailabilityRule rule = new BusinessAvailabilityRule();
        rule.setBusinessProfile(profile);
        rule.setBusinessOffering(offering);
        rule.setDayOfWeek(1);
        rule.setStartTimeLocal(LocalTime.of(8, 0));
        rule.setEndTimeLocal(LocalTime.of(12, 0));
        rule.setSlotGranularityMinutes(60);
        return rule;
    }
}
