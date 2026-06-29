package com.themuffinman.app.common.concepts;

import java.time.Instant;

public record SchedulingWindow(Instant startsAt, Instant endsAt) {

    public boolean hasStart() {
        return startsAt != null;
    }

    public boolean hasEnd() {
        return endsAt != null;
    }

    public boolean startsBefore(Instant instant) {
        return startsAt != null && instant != null && startsAt.isBefore(instant);
    }

    public boolean hasEndWithoutStart() {
        return endsAt != null && startsAt == null;
    }

    public boolean hasInvalidRange() {
        return startsAt != null && endsAt != null && !endsAt.isAfter(startsAt);
    }
}
