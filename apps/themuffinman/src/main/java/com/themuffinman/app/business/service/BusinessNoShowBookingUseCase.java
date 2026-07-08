package com.themuffinman.app.business.service;

import com.themuffinman.app.business.event.BusinessBookingStatusChangedEvent;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessNoShowBookingUseCase {

    private final BusinessBookingRepository businessBookingRepository;
    private final BusinessBookingValidationService businessBookingValidationService;
    private final BusinessBookingMgr businessBookingMgr;
    private final BusinessBookingPresentationService businessBookingPresentationService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public com.themuffinman.app.business.dto.BusinessBookingResponseDTO markNoShow(Long bookingId, AppUser currentUser) {
        BusinessBooking booking = businessBookingRepository.findDetailedByIdAndOwnerId(bookingId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Booking not found"));
        businessBookingValidationService.validateOwnerNoShow(booking, TimeSupport.now());

        BusinessBookingStatus previousStatus = booking.getStatus();
        booking.setStatus(BusinessBookingStatus.NO_SHOW);
        BusinessBooking saved = businessBookingRepository.save(booking);
        domainEventPublisher.publish(new BusinessBookingStatusChangedEvent(saved, previousStatus, saved.getStatus(), currentUser, booking.getOwnerNote()));
        return businessBookingPresentationService.enrichForOwner(businessBookingMgr.toDto(saved), saved, currentUser);
    }
}
