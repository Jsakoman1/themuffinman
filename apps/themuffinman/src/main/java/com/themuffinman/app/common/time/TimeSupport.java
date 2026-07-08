package com.themuffinman.app.common.time;

import com.themuffinman.app.common.errors.ServiceErrors;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class TimeSupport {

    private TimeSupport() {
    }

    public static Instant now() {
        return Instant.now();
    }

    public static Instant now(Clock clock) {
        return clock == null ? Instant.now() : Instant.now(clock);
    }

    public static ZoneId requireZoneId(String timezone, String message) {
        if (timezone == null || timezone.isBlank()) {
            throw ServiceErrors.badRequest(message);
        }
        return ZoneId.of(timezone.trim());
    }

    public static ZoneId resolveZoneIdOrDefault(String timezone, ZoneId fallback) {
        if (timezone == null || timezone.isBlank()) {
            return fallback;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    public static LocalDate today(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    public static Instant startOfDay(LocalDate date, ZoneId zoneId) {
        return date.atStartOfDay(zoneId).toInstant();
    }

    public static Instant endOfDay(LocalDate date, ZoneId zoneId) {
        return date.plusDays(1).atStartOfDay(zoneId).toInstant();
    }

    public static Instant plusSeconds(long seconds) {
        return now().plusSeconds(seconds);
    }

    public static Instant minusSeconds(long seconds) {
        return now().minusSeconds(seconds);
    }

    public static Instant daysAgo(long days) {
        return now().minus(Duration.ofDays(days));
    }
}
