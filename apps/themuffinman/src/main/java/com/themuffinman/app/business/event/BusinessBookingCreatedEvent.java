package com.themuffinman.app.business.event;

import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.common.event.DomainEvent;
import com.themuffinman.app.identity.model.AppUser;

public record BusinessBookingCreatedEvent(
        BusinessBooking booking,
        AppUser actor,
        String note
) implements DomainEvent {
}
