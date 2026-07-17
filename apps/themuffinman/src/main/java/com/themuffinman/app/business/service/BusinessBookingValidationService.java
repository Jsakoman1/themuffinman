package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessAvailabilityWindowDTO;
import com.themuffinman.app.business.model.BusinessAvailabilityException;
import com.themuffinman.app.business.model.BusinessAvailabilityRule;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessOfferingDurationMode;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessAvailabilityExceptionRepository;
import com.themuffinman.app.business.repository.BusinessAvailabilityRuleRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessBookingValidationService {

    private final BusinessAvailabilityRuleRepository businessAvailabilityRuleRepository;
    private final BusinessAvailabilityExceptionRepository businessAvailabilityExceptionRepository;
    private final BusinessAvailabilityComputationService businessAvailabilityComputationService;
    private final BusinessBookingPrimitiveService businessBookingPrimitiveService;

    public CapacityDecision validateCreate(
            BusinessOffering offering,
            AppUser customerUser,
            Instant startsAt,
            Instant endsAt,
            BusinessBookingPolicy policy
    ) {
        BusinessProfile profile = offering.getBusinessProfile();
        requireBookableProfile(profile);
        if (!offering.isActive()) {
            throw ServiceErrors.conflict("Business offering is not active");
        }
        if (customerUser.getId().equals(profile.getOwner().getId())) {
            throw ServiceErrors.forbidden("You cannot book your own business");
        }

        validateTimeRange(offering, startsAt, endsAt, policy);
        List<BusinessAvailabilityWindowDTO> coverage = requireAvailabilityCoverage(profile, offering, startsAt, endsAt);
        int effectiveCapacity = coverage.stream()
                .map(BusinessAvailabilityWindowDTO::getEffectiveCapacity)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> ServiceErrors.conflict("Requested booking slot is not available"));
        long occupied = businessBookingPrimitiveService.countOverlappingCapacityUsage(offering.getId(), startsAt, endsAt);
        if (occupied >= effectiveCapacity) {
            throw ServiceErrors.conflict("Requested booking slot is already full");
        }

        return new CapacityDecision(effectiveCapacity, occupied);
    }

    public void validateReschedule(
            BusinessBooking booking,
            BusinessOffering offering,
            Instant startsAt,
            Instant endsAt,
            BusinessBookingPolicy policy,
            long occupiedExcludingCurrent
    ) {
        if (booking.getStatus() != BusinessBookingStatus.PENDING_CONFIRMATION
                && booking.getStatus() != BusinessBookingStatus.CONFIRMED) {
            throw ServiceErrors.conflict("Booking cannot be rescheduled in its current status");
        }
        validateTimeRange(offering, startsAt, endsAt, policy);
        List<BusinessAvailabilityWindowDTO> coverage = requireAvailabilityCoverage(
                offering.getBusinessProfile(), offering, startsAt, endsAt);
        int effectiveCapacity = coverage.stream()
                .map(BusinessAvailabilityWindowDTO::getEffectiveCapacity)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> ServiceErrors.conflict("Requested booking slot is not available"));
        if (occupiedExcludingCurrent >= effectiveCapacity) {
            throw ServiceErrors.conflict("Requested booking slot is already full");
        }
    }

    public void validateCustomerCancellation(BusinessBooking booking, BusinessBookingPolicy policy, Instant now) {
        if (!policy.isAllowCustomerCancellation()) {
            throw ServiceErrors.conflict("Customer cancellation is disabled for this business");
        }
        if (booking.getStatus() != BusinessBookingStatus.PENDING_CONFIRMATION
                && booking.getStatus() != BusinessBookingStatus.CONFIRMED) {
            throw ServiceErrors.conflict("Booking cannot be cancelled in its current status");
        }
        Instant latestCustomerCancellation = booking.getStartsAt()
                .minus(Duration.ofMinutes(policy.getCustomerCancellationWindowMinutes()));
        if (!latestCustomerCancellation.isAfter(now)) {
            throw ServiceErrors.conflict("Customer cancellation window has closed");
        }
    }

    public void validateOwnerCompletion(BusinessBooking booking, Instant now) {
        if (booking.getStatus() != BusinessBookingStatus.CONFIRMED) {
            throw ServiceErrors.conflict("Only confirmed bookings can be completed");
        }
        if (booking.getEndsAt().isAfter(now)) {
            throw ServiceErrors.conflict("Booking can only be completed after it ends");
        }
    }

    public void validateOwnerNoShow(BusinessBooking booking, Instant now) {
        if (booking.getStatus() != BusinessBookingStatus.CONFIRMED) {
            throw ServiceErrors.conflict("Only confirmed bookings can be marked as no-show");
        }
        if (booking.getStartsAt().isAfter(now)) {
            throw ServiceErrors.conflict("Booking can only be marked as no-show after it starts");
        }
    }

    private void validateTimeRange(BusinessOffering offering, Instant startsAt, Instant endsAt, BusinessBookingPolicy policy) {
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw ServiceErrors.badRequest("Booking time range is invalid");
        }

        Instant now = TimeSupport.now();
        if (startsAt.isBefore(now.plus(Duration.ofMinutes(policy.getLeadTimeMinutes())))) {
            throw ServiceErrors.conflict("Booking is inside the lead time restriction");
        }
        if (startsAt.isAfter(now.plus(Duration.ofDays(policy.getMaxAdvanceDays())))) {
            throw ServiceErrors.conflict("Booking is outside the max advance horizon");
        }

        long durationMinutes = Duration.between(startsAt, endsAt).toMinutes();
        if (durationMinutes < 1) {
            throw ServiceErrors.badRequest("Booking duration must be at least one minute");
        }

        if (offering.getDurationMode() == BusinessOfferingDurationMode.FIXED
                && !durationMinutesEquals(durationMinutes, offering.getDefaultDurationMinutes())) {
            throw ServiceErrors.conflict("Booking duration must match the offering duration");
        }
        if (offering.getDurationMode() == BusinessOfferingDurationMode.CUSTOMER_SELECTS) {
            if (offering.getMinDurationMinutes() != null && durationMinutes < offering.getMinDurationMinutes()) {
                throw ServiceErrors.conflict("Booking duration is shorter than the offering minimum");
            }
            if (offering.getMaxDurationMinutes() != null && durationMinutes > offering.getMaxDurationMinutes()) {
                throw ServiceErrors.conflict("Booking duration is longer than the offering maximum");
            }
        }
        if (offering.getDurationMode() == BusinessOfferingDurationMode.ALL_DAY) {
            ZoneId zoneId = TimeSupport.requireZoneId(
                    offering.getBusinessProfile().getTimezone(),
                    "Business timezone is required before booking"
            );
            ZonedDateTime startLocal = startsAt.atZone(zoneId);
            ZonedDateTime endLocal = endsAt.atZone(zoneId);
            boolean fullDay = startLocal.toLocalTime().equals(java.time.LocalTime.MIDNIGHT)
                    && endLocal.toLocalTime().equals(java.time.LocalTime.MIDNIGHT)
                    && Duration.between(startLocal, endLocal).toHours() >= 24;
            if (!fullDay) {
                throw ServiceErrors.conflict("All-day offerings require full-day local booking times");
            }
        }
    }

    private List<BusinessAvailabilityWindowDTO> requireAvailabilityCoverage(
            BusinessProfile profile,
            BusinessOffering offering,
            Instant startsAt,
            Instant endsAt
    ) {
        List<BusinessAvailabilityRule> rules = businessAvailabilityRuleRepository.findActiveByBusinessProfileId(profile.getId());
        List<BusinessAvailabilityException> exceptions = businessAvailabilityExceptionRepository.findByBusinessProfileId(profile.getId());
        List<BusinessAvailabilityWindowDTO> windows = businessAvailabilityComputationService.deriveWindows(
                profile,
                offering,
                rules,
                exceptions,
                startsAt,
                endsAt
        );

        Instant cursor = startsAt;
        while (cursor.isBefore(endsAt)) {
            Instant expectedCursor = cursor;
            BusinessAvailabilityWindowDTO nextWindow = windows.stream()
                    .filter(window -> window.getStartsAt().equals(expectedCursor))
                    .findFirst()
                    .orElseThrow(() -> ServiceErrors.conflict("Requested booking slot is not available"));
            if (nextWindow.getEndsAt().isAfter(endsAt)) {
                throw ServiceErrors.conflict("Requested booking slot is not aligned to business availability");
            }
            cursor = nextWindow.getEndsAt();
        }

        return windows.stream()
                .filter(window -> !window.getStartsAt().isBefore(startsAt) && !window.getEndsAt().isAfter(endsAt))
                .toList();
    }

    private void requireBookableProfile(BusinessProfile profile) {
        if (profile == null || !profile.isActive()) {
            throw ServiceErrors.notFound("Business profile not found");
        }
        if (!profile.isBookingEnabled()) {
            throw ServiceErrors.conflict("Business booking is disabled");
        }
        if (profile.getTimezone() == null || profile.getTimezone().isBlank()) {
            throw ServiceErrors.conflict("Business timezone is required before booking");
        }
    }

    private boolean durationMinutesEquals(long durationMinutes, Integer configuredMinutes) {
        return configuredMinutes != null && durationMinutes == configuredMinutes.longValue();
    }

    public record CapacityDecision(int effectiveCapacity, long occupiedCount) {
    }
}
