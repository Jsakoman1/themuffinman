package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingRescheduleRequestDTO;
import com.themuffinman.app.business.event.BusinessBookingRescheduledEvent;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BusinessRescheduleBookingUseCase {
    private final BusinessBookingRepository bookingRepository;
    private final BusinessBookingPrimitiveService bookingPrimitiveService;
    private final BusinessBookingPolicyService bookingPolicyService;
    private final BusinessBookingValidationService bookingValidationService;
    private final BusinessBookingMgr bookingMgr;
    private final BusinessBookingPresentationService presentationService;
    private final DomainEventPublisher domainEventPublisher;
    private final BusinessResourceAssignmentService resourceAssignmentService;

    @Transactional
    public com.themuffinman.app.business.dto.BusinessBookingResponseDTO rescheduleAsCustomer(
            Long bookingId, BusinessBookingRescheduleRequestDTO request, AppUser actor) {
        BusinessBooking booking = bookingRepository.findDetailedByIdAndCustomerUserId(bookingId, actor.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Booking not found"));
        BusinessBookingPolicy policy = bookingPolicyService.loadOrCreatePolicyEntity(booking.getBusinessProfile().getOwner());
        requireWindow(booking, policy.getCustomerCancellationWindowMinutes());
        return reschedule(booking, request, actor, policy, false);
    }

    @Transactional
    public com.themuffinman.app.business.dto.BusinessBookingResponseDTO rescheduleAsOwner(
            Long bookingId, BusinessBookingRescheduleRequestDTO request, AppUser actor) {
        BusinessBooking booking = bookingRepository.findDetailedByIdAndOwnerId(bookingId, actor.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Booking not found"));
        BusinessBookingPolicy policy = bookingPolicyService.loadOrCreatePolicyEntity(actor);
        requireWindow(booking, policy.getOwnerRescheduleWindowMinutes());
        return reschedule(booking, request, actor, policy, true);
    }

    private com.themuffinman.app.business.dto.BusinessBookingResponseDTO reschedule(
            BusinessBooking booking, BusinessBookingRescheduleRequestDTO request, AppUser actor,
            BusinessBookingPolicy policy, boolean ownerView) {
        if (request == null) {
            throw ServiceErrors.badRequest("Reschedule request is required");
        }
        var offering = bookingPrimitiveService.lockOffering(booking.getBusinessOffering().getId());
        BigDecimal occupied = bookingPrimitiveService.countOverlappingCapacityUsageExcluding(
                offering.getId(), booking.getId(), request.getStartsAt(), request.getEndsAt());
        bookingValidationService.validateReschedule(
                booking, offering, request.getStartsAt(), request.getEndsAt(), policy, occupied);
        // Reassignment happens before the booking interval is persisted so a conflict rolls back the whole reschedule.
        if (resourceAssignmentService != null) {
            resourceAssignmentService.reassignForBooking(
                    booking.getId(), offering, request.getStartsAt(), request.getEndsAt());
        }
        booking.setStartsAt(request.getStartsAt());
        booking.setEndsAt(request.getEndsAt());
        booking.setDurationSnapshotMinutes((int) Duration.between(request.getStartsAt(), request.getEndsAt()).toMinutes());
        BusinessBooking saved = bookingRepository.save(booking);
        domainEventPublisher.publish(new BusinessBookingRescheduledEvent(saved, actor, request.getReason()));
        var dto = bookingMgr.toDto(saved);
        return ownerView
                ? presentationService.enrichForOwner(dto, saved, actor)
                : presentationService.enrichForCustomer(dto, saved, actor);
    }

    private void requireWindow(BusinessBooking booking, int windowMinutes) {
        if (!booking.getStartsAt().minus(Duration.ofMinutes(windowMinutes)).isAfter(TimeSupport.now())) {
            throw ServiceErrors.conflict("Booking reschedule window has closed");
        }
    }
}
