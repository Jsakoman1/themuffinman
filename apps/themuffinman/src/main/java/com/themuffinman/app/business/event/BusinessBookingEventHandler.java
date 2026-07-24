package com.themuffinman.app.business.event;

import com.themuffinman.app.business.service.BusinessBookingAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessBookingEventHandler {

    private final BusinessBookingAuditService businessBookingAuditService;

    @EventListener
    public void handle(BusinessBookingCreatedEvent event) {
        businessBookingAuditService.recordCreated(event.booking(), event.actor(), normalizeNote(event.note()));
    }

    @EventListener
    public void handle(BusinessBookingStatusChangedEvent event) {
        businessBookingAuditService.recordStatusChange(
                event.booking(),
                event.previousStatus(),
                event.newStatus(),
                event.actor(),
                normalizeNote(event.note())
        );
    }

    @EventListener
    public void handle(BusinessBookingRescheduledEvent event) {
        businessBookingAuditService.recordRescheduled(event.booking(), event.actor(), normalizeNote(event.note()));
    }

    private String normalizeNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }
}
