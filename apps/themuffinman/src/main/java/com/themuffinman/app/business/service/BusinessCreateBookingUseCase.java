package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingRequestDTO;
import com.themuffinman.app.business.dto.BusinessOwnerBookingCreateRequestDTO;
import com.themuffinman.app.business.event.BusinessBookingCreatedEvent;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingSnapshot;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessBookingSource;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOfferingBookingMode;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.business.repository.BusinessBookingSnapshotRepository;
import com.themuffinman.app.business.dto.BusinessQuoteRequestDTO;
import com.themuffinman.app.business.dto.BusinessQuoteResponseDTO;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.common.reliability.MutationIdempotencyService;
import com.themuffinman.app.common.reliability.MutationOperationPolicy;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final BusinessBookingSnapshotRepository businessBookingSnapshotRepository;
    private final AppUserRepository appUserRepository;
    private final BusinessBookingMgr businessBookingMgr;
    private final BusinessBookingPresentationService businessBookingPresentationService;
    private final DomainEventPublisher domainEventPublisher;
    private final MutationIdempotencyService mutationIdempotencyService;
    private final BusinessPricingCalculationService businessPricingCalculationService;
    private final BusinessOfferingSchemaService businessOfferingSchemaService;
    private final BusinessResourceAssignmentService businessResourceAssignmentService;

    private static final MutationOperationPolicy CUSTOMER_BOOKING_POLICY =
            new MutationOperationPolicy("business.booking.create.customer", false, false, true);
    private static final MutationOperationPolicy OWNER_BOOKING_POLICY =
            new MutationOperationPolicy("business.booking.create.owner", false, false, true);

    @Transactional
    public com.themuffinman.app.business.dto.BusinessBookingResponseDTO createCustomerBooking(
            BusinessBookingRequestDTO dto,
            AppUser currentUser
    ) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Booking request is required");
        }
        String idempotencyKey = mutationIdempotencyService.requireKey(dto.getIdempotencyKey(), CUSTOMER_BOOKING_POLICY);
        if (idempotencyKey != null) {
            BusinessBooking existing = businessBookingRepository.findByCustomerUserIdAndIdempotencyKey(currentUser.getId(), idempotencyKey)
                    .orElse(null);
            if (existing != null) {
                validateSamePayload(existing, dto.getBusinessOfferingId(), dto.getStartsAt(), dto.getEndsAt());
                return businessBookingPresentationService.enrichForCustomer(businessBookingMgr.toDto(existing), existing, currentUser);
            }
        }

        BusinessOffering offering = businessBookingPrimitiveService.lockOffering(dto.getBusinessOfferingId());
        dto.setEndsAt(resolveEndsAt(offering, dto));
        if (businessOfferingSchemaService != null) {
            businessOfferingSchemaService.validateCustomerInput(offering.getId(), dto.getAnswers(), dto.getSelectedOptions());
        }
        BusinessBookingPolicy policy = businessBookingPolicyService.loadOrCreatePolicyEntity(offering.getBusinessProfile().getOwner());
        businessBookingValidationService.validateCreate(offering, currentUser, dto.getStartsAt(), dto.getEndsAt(), policy, dto.getQuantity());

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
        booking.setIdempotencyKey(idempotencyKey);
        booking.setQuantitySnapshot(dto.getQuantity() == null ? java.math.BigDecimal.ONE : dto.getQuantity());
        BusinessQuoteResponseDTO quote = calculateQuote(offering, dto.getStartsAt(), dto.getEndsAt(), dto.getQuantity(), dto.getAnswers(), dto.getSelectedOptions());
        applySnapshots(booking, offering, quote);

        BusinessBooking saved = businessBookingRepository.save(booking);
        java.util.List<java.util.Map<String, Object>> assignments = businessResourceAssignmentService == null ? java.util.List.of()
                : businessResourceAssignmentService.assignForBooking(saved.getId(), offering, saved.getStartsAt(), saved.getEndsAt());
        businessBookingSnapshotRepository.save(createSnapshot(saved, offering, dto.getAnswers(), dto.getSelectedOptions(), quote, assignments));
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
        String idempotencyKey = mutationIdempotencyService.requireKey(dto.getIdempotencyKey(), OWNER_BOOKING_POLICY);
        if (idempotencyKey != null) {
            BusinessBooking existing = businessBookingRepository.findByBusinessProfileOwnerIdAndIdempotencyKey(currentUser.getId(), idempotencyKey)
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
        businessBookingValidationService.validateCreate(offering, customer, dto.getStartsAt(), dto.getEndsAt(), policy, dto.getQuantity());

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
        booking.setIdempotencyKey(idempotencyKey);
        booking.setQuantitySnapshot(dto.getQuantity() == null ? java.math.BigDecimal.ONE : dto.getQuantity());
        BusinessQuoteResponseDTO quote = calculateQuote(offering, dto.getStartsAt(), dto.getEndsAt(), dto.getQuantity(), java.util.Map.of(), java.util.Map.of());
        applySnapshots(booking, offering, quote);

        BusinessBooking saved = businessBookingRepository.save(booking);
        java.util.List<java.util.Map<String, Object>> assignments = businessResourceAssignmentService == null ? java.util.List.of()
                : businessResourceAssignmentService.assignForBooking(saved.getId(), offering, saved.getStartsAt(), saved.getEndsAt());
        businessBookingSnapshotRepository.save(createSnapshot(saved, offering, java.util.Map.of(), java.util.Map.of(), quote, assignments));
        domainEventPublisher.publish(new BusinessBookingCreatedEvent(saved, currentUser, dto.getOwnerNote()));
        return businessBookingPresentationService.enrichForOwner(businessBookingMgr.toDto(saved), saved, currentUser);
    }

    private void applySnapshots(BusinessBooking booking, BusinessOffering offering, BusinessQuoteResponseDTO quote) {
        booking.setOfferingTitleSnapshot(offering.getTitle());
        booking.setPriceSnapshotAmount(quote == null ? offering.getBasePriceAmount() : quote.getTotalAmount());
        booking.setPriceSnapshotCurrency(offering.getBasePriceCurrency());
        booking.setDurationSnapshotMinutes((int) Duration.between(booking.getStartsAt(), booking.getEndsAt()).toMinutes());
    }

    private BusinessBookingSnapshot createSnapshot(
            BusinessBooking booking,
            BusinessOffering offering,
            java.util.Map<String, String> answers,
            java.util.Map<String, String> selectedOptions,
            BusinessQuoteResponseDTO quote,
            java.util.List<java.util.Map<String, Object>> assignments
    ) {
        BusinessBookingSnapshot snapshot = new BusinessBookingSnapshot();
        snapshot.setBusinessBooking(booking);
        snapshot.setOfferingSchemaVersion(offering.getSchemaVersion());
        snapshot.setFulfillmentMode(offering.getFulfillmentMode());
        snapshot.setDemand(toJson(java.util.Map.of(
                "quantity", booking.getQuantitySnapshot(),
                "durationMinutes", booking.getDurationSnapshotMinutes(),
                "answers", answers == null ? java.util.Map.of() : answers
        )));
        snapshot.setSelectedOptions(toJson(selectedOptions == null ? java.util.Map.of() : selectedOptions));
        String amount = quote == null || quote.getTotalAmount() == null ? "null" : "\"" + quote.getTotalAmount().toPlainString() + "\"";
        String currency = offering.getBasePriceCurrency() == null ? "null" : "\"" + offering.getBasePriceCurrency() + "\"";
        java.util.Map<String, Object> priceLine = new java.util.LinkedHashMap<>();
        priceLine.put("type", "calculated");
        priceLine.put("amount", quote == null ? offering.getBasePriceAmount() : quote.getTotalAmount());
        priceLine.put("currency", offering.getBasePriceCurrency());
        priceLine.put("explanations", quote == null ? java.util.List.of() : quote.getExplanations());
        snapshot.setPriceLines(toJson(java.util.List.of(priceLine)));
        snapshot.setResourceAssignments(toJson(assignments == null ? java.util.List.of() : assignments));
        snapshot.setCapacityConsumption("{\"units\":" + booking.getQuantitySnapshot().toPlainString() + "}");
        snapshot.setConditionsSnapshot("{\"timezone\":\"" + booking.getTimezone() + "\"}");
        return snapshot;
    }

    private BusinessQuoteResponseDTO calculateQuote(
            BusinessOffering offering,
            java.time.Instant startsAt,
            java.time.Instant endsAt,
            java.math.BigDecimal quantity,
            java.util.Map<String, String> answers,
            java.util.Map<String, String> selectedOptions
    ) {
        if (businessPricingCalculationService == null) return null;
        BusinessQuoteRequestDTO request = new BusinessQuoteRequestDTO();
        request.setBusinessOfferingId(offering.getId());
        request.setStartsAt(startsAt);
        request.setDurationMinutes((int) java.time.Duration.between(startsAt, endsAt).toMinutes());
        request.setQuantity(quantity);
        request.setAnswers(answers);
        request.setSelectedOptions(selectedOptions);
        return businessPricingCalculationService.calculate(offering, request);
    }

    private String toJson(Object value) {
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw ServiceErrors.badRequest("Booking demand could not be snapshotted");
        }
    }

    private java.time.Instant resolveEndsAt(BusinessOffering offering, BusinessBookingRequestDTO dto) {
        if (dto.getEndsAt() != null) {
            return dto.getEndsAt();
        }
        if (dto.getStartsAt() == null || offering.getDefaultDurationMinutes() == null) {
            throw ServiceErrors.badRequest("Booking end or offering duration is required");
        }
        return dto.getStartsAt().plus(java.time.Duration.ofMinutes(offering.getDefaultDurationMinutes()));
    }

    private void validateSamePayload(BusinessBooking existing, Long offeringId, java.time.Instant startsAt, java.time.Instant endsAt) {
        java.time.Instant effectiveEndsAt = endsAt;
        if (effectiveEndsAt == null && startsAt != null && existing.getDurationSnapshotMinutes() != null) {
            effectiveEndsAt = startsAt.plus(java.time.Duration.ofMinutes(existing.getDurationSnapshotMinutes()));
        }
        if (!Objects.equals(existing.getBusinessOffering().getId(), offeringId)
                || !Objects.equals(existing.getStartsAt(), startsAt)
                || !Objects.equals(existing.getEndsAt(), effectiveEndsAt)) {
            throw ServiceErrors.conflict("Idempotency key is already used for a different booking request");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
