package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingRequestDTO;
import com.themuffinman.app.business.dto.BusinessOwnerBookingCreateRequestDTO;
import com.themuffinman.app.business.event.BusinessBookingCreatedEvent;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessBookingSource;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOfferingBookingMode;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BusinessCreateBookingUseCase {

    private final BusinessBookingPrimitiveService businessBookingPrimitiveService;
    private final BusinessBookingValidationService businessBookingValidationService;
    private final BusinessBookingPolicyService businessBookingPolicyService;
    private final BusinessBookingRepository businessBookingRepository;
    private final AppUserRepository appUserRepository;
    private final BusinessBookingMgr businessBookingMgr;
    private final BusinessBookingPresentationService businessBookingPresentationService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public com.themuffinman.app.business.dto.BusinessBookingResponseDTO createCustomerBooking(
            BusinessBookingRequestDTO dto,
            AppUser currentUser
    ) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Booking request is required");
        }
        if (dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().isBlank()) {
            BusinessBooking existing = businessBookingRepository.findByCustomerUserIdAndIdempotencyKey(currentUser.getId(), dto.getIdempotencyKey())
                    .orElse(null);
            if (existing != null) {
                validateSamePayload(existing, dto.getBusinessOfferingId(), dto.getStartsAt(), dto.getEndsAt());
                return businessBookingPresentationService.enrichForCustomer(businessBookingMgr.toDto(existing), existing, currentUser);
            }
        }

        BusinessOffering offering = businessBookingPrimitiveService.lockOffering(dto.getBusinessOfferingId());
        BusinessBookingPolicy policy = businessBookingPolicyService.loadOrCreatePolicyEntity(offering.getBusinessProfile().getOwner());
        businessBookingValidationService.validateCreate(offering, currentUser, dto.getStartsAt(), dto.getEndsAt(), policy);

        BusinessBooking booking = new BusinessBooking();
        booking.setBusinessProfile(offering.getBusinessProfile());
        booking.setBusinessOffering(offering);
        booking.setCustomerUser(currentUser);
        booking.setStatus(offering.getBookingMode() == BusinessOfferingBookingMode.INSTANT
                ? BusinessBookingStatus.CONFIRMED
                : BusinessBookingStatus.PENDING_CONFIRMATION);
        booking.setSource(BusinessBookingSource.CUSTOMER);
        booking.setStartsAt(dto.getStartsAt());
        booking.setEndsAt(dto.getEndsAt());
        booking.setTimezone(offering.getBusinessProfile().getTimezone());
        booking.setCustomerNote(dto.getCustomerNote());
        booking.setIdempotencyKey(blankToNull(dto.getIdempotencyKey()));
        applySnapshots(booking, offering);

        BusinessBooking saved = businessBookingRepository.save(booking);
        domainEventPublisher.publish(new BusinessBookingCreatedEvent(saved, currentUser, dto.getCustomerNote()));
        return businessBookingPresentationService.enrichForCustomer(businessBookingMgr.toDto(saved), saved, currentUser);
    }

    @Transactional
    public com.themuffinman.app.business.dto.BusinessBookingResponseDTO createOwnerBooking(
            BusinessOwnerBookingCreateRequestDTO dto,
            AppUser currentUser
    ) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Owner booking request is required");
        }
        if (dto.getIdempotencyKey() != null && !dto.getIdempotencyKey().isBlank()) {
            BusinessBooking existing = businessBookingRepository.findByBusinessProfileOwnerIdAndIdempotencyKey(currentUser.getId(), dto.getIdempotencyKey())
                    .orElse(null);
            if (existing != null) {
                validateSamePayload(existing, dto.getBusinessOfferingId(), dto.getStartsAt(), dto.getEndsAt());
                return businessBookingPresentationService.enrichForOwner(businessBookingMgr.toDto(existing), existing, currentUser);
            }
        }

        BusinessOffering offering = businessBookingPrimitiveService.lockOffering(dto.getBusinessOfferingId());
        if (!offering.getBusinessProfile().getOwner().getId().equals(currentUser.getId())) {
            throw ServiceErrors.forbidden("You can only create owner bookings for your own business");
        }
        AppUser customer = appUserRepository.findById(dto.getCustomerUserId())
                .orElseThrow(() -> ServiceErrors.notFound("Customer user not found"));
        BusinessBookingPolicy policy = businessBookingPolicyService.loadOrCreatePolicyEntity(currentUser);
        businessBookingValidationService.validateCreate(offering, customer, dto.getStartsAt(), dto.getEndsAt(), policy);

        BusinessBooking booking = new BusinessBooking();
        booking.setBusinessProfile(offering.getBusinessProfile());
        booking.setBusinessOffering(offering);
        booking.setCustomerUser(customer);
        booking.setStatus(BusinessBookingStatus.CONFIRMED);
        booking.setSource(BusinessBookingSource.OWNER_CREATED);
        booking.setStartsAt(dto.getStartsAt());
        booking.setEndsAt(dto.getEndsAt());
        booking.setTimezone(offering.getBusinessProfile().getTimezone());
        booking.setOwnerNote(dto.getOwnerNote());
        booking.setIdempotencyKey(blankToNull(dto.getIdempotencyKey()));
        applySnapshots(booking, offering);

        BusinessBooking saved = businessBookingRepository.save(booking);
        domainEventPublisher.publish(new BusinessBookingCreatedEvent(saved, currentUser, dto.getOwnerNote()));
        return businessBookingPresentationService.enrichForOwner(businessBookingMgr.toDto(saved), saved, currentUser);
    }

    private void applySnapshots(BusinessBooking booking, BusinessOffering offering) {
        booking.setOfferingTitleSnapshot(offering.getTitle());
        booking.setPriceSnapshotAmount(offering.getBasePriceAmount());
        booking.setPriceSnapshotCurrency(offering.getBasePriceCurrency());
        booking.setDurationSnapshotMinutes((int) Duration.between(booking.getStartsAt(), booking.getEndsAt()).toMinutes());
    }

    private void validateSamePayload(BusinessBooking existing, Long offeringId, java.time.Instant startsAt, java.time.Instant endsAt) {
        if (!Objects.equals(existing.getBusinessOffering().getId(), offeringId)
                || !Objects.equals(existing.getStartsAt(), startsAt)
                || !Objects.equals(existing.getEndsAt(), endsAt)) {
            throw ServiceErrors.conflict("Idempotency key is already used for a different booking request");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
