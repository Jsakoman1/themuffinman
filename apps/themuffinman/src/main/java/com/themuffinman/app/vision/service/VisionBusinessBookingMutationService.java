package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.service.BusinessCancelBookingUseCase;
import com.themuffinman.app.business.service.BusinessConfirmBookingUseCase;
import com.themuffinman.app.business.service.BusinessRejectBookingUseCase;
import com.themuffinman.app.business.service.BusinessCompleteBookingUseCase;
import com.themuffinman.app.business.service.BusinessNoShowBookingUseCase;
import com.themuffinman.app.business.service.BusinessRescheduleBookingUseCase;
import com.themuffinman.app.business.dto.BusinessBookingRescheduleRequestDTO;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisionBusinessBookingMutationService {
    private final BusinessBookingRepository repository;
    private final BusinessConfirmBookingUseCase confirmBookingUseCase;
    private final BusinessCancelBookingUseCase cancelBookingUseCase;
    private final BusinessRejectBookingUseCase rejectBookingUseCase;
    private final BusinessCompleteBookingUseCase completeBookingUseCase;
    private final BusinessNoShowBookingUseCase noShowBookingUseCase;
    private final BusinessRescheduleBookingUseCase rescheduleBookingUseCase;

    @Transactional
    public BusinessBookingResponseDTO confirm(Long bookingId, AppUser currentUser) {
        return confirmBookingUseCase.execute(bookingId, currentUser);
    }

    @Transactional
    public BusinessBookingResponseDTO cancel(Long bookingId, AppUser currentUser) {
        if (repository.findDetailedByIdAndCustomerUserId(bookingId, currentUser.getId()).isPresent()) {
            return cancelBookingUseCase.cancelAsCustomer(bookingId, currentUser);
        }
        if (repository.findDetailedByIdAndOwnerId(bookingId, currentUser.getId()).isPresent()) {
            return cancelBookingUseCase.cancelAsOwner(bookingId, currentUser);
        }
        throw ServiceErrors.notFound("Booking not found");
    }

    @Transactional public BusinessBookingResponseDTO reject(Long bookingId, AppUser currentUser) { return rejectBookingUseCase.execute(bookingId, currentUser); }
    @Transactional public BusinessBookingResponseDTO complete(Long bookingId, AppUser currentUser) { return completeBookingUseCase.complete(bookingId, currentUser); }
    @Transactional public BusinessBookingResponseDTO noShow(Long bookingId, AppUser currentUser) { return noShowBookingUseCase.markNoShow(bookingId, currentUser); }
    @Transactional public BusinessBookingResponseDTO reschedule(Long bookingId, String startsAt, String endsAt, AppUser currentUser) {
        BusinessBookingRescheduleRequestDTO request = new BusinessBookingRescheduleRequestDTO();
        try {
            request.setStartsAt(java.time.Instant.parse(startsAt));
            request.setEndsAt(java.time.Instant.parse(endsAt));
        } catch (RuntimeException exception) {
            throw ServiceErrors.badRequest("Booking start and end must be ISO timestamps");
        }
        if (repository.findDetailedByIdAndCustomerUserId(bookingId, currentUser.getId()).isPresent()) return rescheduleBookingUseCase.rescheduleAsCustomer(bookingId, request, currentUser);
        if (repository.findDetailedByIdAndOwnerId(bookingId, currentUser.getId()).isPresent()) return rescheduleBookingUseCase.rescheduleAsOwner(bookingId, request, currentUser);
        throw ServiceErrors.notFound("Booking not found");
    }
}
