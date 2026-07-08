package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingAuditEvent;
import com.themuffinman.app.business.model.BusinessBookingAuditEventType;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.repository.BusinessBookingAuditEventRepository;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessBookingAuditService {

    private final BusinessBookingAuditEventRepository businessBookingAuditEventRepository;

    @Transactional
    public void recordCreated(BusinessBooking booking, AppUser actor, String note) {
        BusinessBookingAuditEvent event = new BusinessBookingAuditEvent();
        event.setBooking(booking);
        event.setEventType(BusinessBookingAuditEventType.CREATED);
        event.setNewStatus(booking.getStatus());
        event.setActorUserId(actor == null ? null : actor.getId());
        event.setActorUsername(actor == null ? null : actor.getUsername());
        event.setNote(note);
        businessBookingAuditEventRepository.save(event);
    }

    @Transactional
    public void recordStatusChange(
            BusinessBooking booking,
            BusinessBookingStatus previousStatus,
            BusinessBookingStatus newStatus,
            AppUser actor,
            String note
    ) {
        BusinessBookingAuditEvent event = new BusinessBookingAuditEvent();
        event.setBooking(booking);
        event.setEventType(mapStatusChange(newStatus));
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setActorUserId(actor == null ? null : actor.getId());
        event.setActorUsername(actor == null ? null : actor.getUsername());
        event.setNote(note);
        businessBookingAuditEventRepository.save(event);
    }

    private BusinessBookingAuditEventType mapStatusChange(BusinessBookingStatus newStatus) {
        return switch (newStatus) {
            case CONFIRMED -> BusinessBookingAuditEventType.CONFIRMED;
            case REJECTED -> BusinessBookingAuditEventType.REJECTED;
            case CANCELLED_BY_CUSTOMER -> BusinessBookingAuditEventType.CANCELLED_BY_CUSTOMER;
            case CANCELLED_BY_OWNER -> BusinessBookingAuditEventType.CANCELLED_BY_OWNER;
            case COMPLETED -> BusinessBookingAuditEventType.COMPLETED;
            case NO_SHOW -> BusinessBookingAuditEventType.NO_SHOW;
            case PENDING_CONFIRMATION -> BusinessBookingAuditEventType.CREATED;
        };
    }
}
