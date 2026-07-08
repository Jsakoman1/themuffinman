package com.themuffinman.app.common.time;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeSupportTest {

    @Test
    void requireZoneIdRejectsBlankValues() {
        assertThrows(ResponseStatusException.class, () -> TimeSupport.requireZoneId(" ", "timezone is required"));
    }

    @Test
    void resolveZoneIdOrDefaultFallsBackWhenBlankOrInvalid() {
        ZoneId fallback = ZoneId.of("UTC");
        assertEquals(fallback, TimeSupport.resolveZoneIdOrDefault(" ", fallback));
        assertEquals(fallback, TimeSupport.resolveZoneIdOrDefault("not-a-zone", fallback));
    }

    @Test
    void dayBoundariesUseZoneSemantics() {
        ZoneId zoneId = ZoneId.of("Europe/Zurich");
        LocalDate today = TimeSupport.today(zoneId);
        assertEquals(today.atStartOfDay(zoneId).toInstant(), TimeSupport.startOfDay(today, zoneId));
        assertEquals(today.plusDays(1).atStartOfDay(zoneId).toInstant(), TimeSupport.endOfDay(today, zoneId));
    }

    @Test
    void nowHelpersReturnInstants() {
        Instant now = TimeSupport.now();
        assertEquals(Instant.class, now.getClass());
        assertEquals(Instant.class, TimeSupport.plusSeconds(60).getClass());
        assertEquals(Instant.class, TimeSupport.minusSeconds(60).getClass());
    }
}
