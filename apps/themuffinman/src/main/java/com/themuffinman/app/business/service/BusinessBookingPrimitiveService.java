package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Service
public class BusinessBookingPrimitiveService {

    private static final Set<BusinessBookingStatus> CAPACITY_CONSUMING_STATUSES =
            EnumSet.of(BusinessBookingStatus.PENDING_CONFIRMATION, BusinessBookingStatus.CONFIRMED);

    private final BusinessOfferingRepository businessOfferingRepository;
    private final BusinessBookingRepository businessBookingRepository;

    public BusinessBookingPrimitiveService(
            BusinessOfferingRepository businessOfferingRepository,
            BusinessBookingRepository businessBookingRepository
    ) {
        this.businessOfferingRepository = businessOfferingRepository;
        this.businessBookingRepository = businessBookingRepository;
    }

    public BusinessOffering lockOffering(Long offeringId) {
        return businessOfferingRepository.findByIdForUpdate(offeringId)
                .orElseThrow(() -> ServiceErrors.notFound("Business offering not found"));
    }

    public long countOverlappingCapacityUsage(Long offeringId, Instant startsAt, Instant endsAt) {
        return businessBookingRepository.countOverlappingBookings(offeringId, CAPACITY_CONSUMING_STATUSES, startsAt, endsAt);
    }

    public Set<BusinessBookingStatus> capacityConsumingStatuses() {
        return CAPACITY_CONSUMING_STATUSES;
    }

    public void requireStatus(BusinessBooking booking, Set<BusinessBookingStatus> allowedStatuses, String message) {
        if (booking == null || !allowedStatuses.contains(booking.getStatus())) {
            throw ServiceErrors.conflict(message);
        }
    }
}
