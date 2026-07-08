package com.themuffinman.app.business.event;

import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.common.event.DomainEvent;
import com.themuffinman.app.identity.model.AppUser;

public record BusinessBookingStatusChangedEvent(
        BusinessBooking booking,
        BusinessBookingStatus previousStatus,
        BusinessBookingStatus newStatus,
        AppUser actor,
        String note
) implements DomainEvent {
}
