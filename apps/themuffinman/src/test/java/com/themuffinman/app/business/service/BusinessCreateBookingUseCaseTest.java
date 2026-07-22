package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessBookingRequestDTO;
import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessOfferingBookingMode;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.common.event.DomainEvent;
import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.common.reliability.MutationIdempotencyService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessCreateBookingUseCaseTest {

    @Mock
    private BusinessBookingPrimitiveService businessBookingPrimitiveService;

    @Mock
    private BusinessBookingValidationService businessBookingValidationService;

    @Mock
    private BusinessBookingPolicyService businessBookingPolicyService;

    @Mock
    private BusinessBookingRepository businessBookingRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Spy
    private BusinessBookingMgr businessBookingMgr = new BusinessBookingMgr();

    @Mock
    private BusinessBookingPresentationService businessBookingPresentationService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Spy
    private MutationIdempotencyService mutationIdempotencyService = new MutationIdempotencyService();

    @InjectMocks
    private BusinessCreateBookingUseCase businessCreateBookingUseCase;

    @Test
    void createCustomerBookingUsesConfirmedStatusForInstantOffering() {
        AppUser owner = user(1L, "owner");
        AppUser customer = user(2L, "customer");
        BusinessOffering offering = offering(owner, BusinessOfferingBookingMode.INSTANT);
        BusinessBookingPolicy policy = policy();
        Instant start = Instant.parse("2026-07-09T09:00:00Z");
        Instant end = Instant.parse("2026-07-09T10:00:00Z");

        when(businessBookingPrimitiveService.lockOffering(offering.getId())).thenReturn(offering);
        when(businessBookingPolicyService.loadOrCreatePolicyEntity(owner)).thenReturn(policy);
        when(businessBookingRepository.save(any(BusinessBooking.class))).thenAnswer(invocation -> {
            BusinessBooking booking = invocation.getArgument(0);
            booking.setId(7L);
            return booking;
        });
        when(businessBookingPresentationService.enrichForCustomer(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = businessCreateBookingUseCase.createCustomerBooking(request(offering.getId(), start, end, "idem-1"), customer);

        assertEquals(7L, result.getId());
        assertEquals(BusinessBookingStatus.CONFIRMED, result.getStatus());
        verify(domainEventPublisher).publish(any(DomainEvent.class));
    }

    @Test
    void createCustomerBookingReturnsExistingBookingForSameIdempotencyKey() {
        AppUser owner = user(1L, "owner");
        AppUser customer = user(2L, "customer");
        BusinessOffering offering = offering(owner, BusinessOfferingBookingMode.REQUEST);
        BusinessBooking existing = new BusinessBooking();
        existing.setId(9L);
        existing.setBusinessProfile(offering.getBusinessProfile());
        existing.setBusinessOffering(offering);
        existing.setCustomerUser(customer);
        existing.setStatus(BusinessBookingStatus.PENDING_CONFIRMATION);
        existing.setStartsAt(Instant.parse("2026-07-09T09:00:00Z"));
        existing.setEndsAt(Instant.parse("2026-07-09T10:00:00Z"));
        existing.setTimezone("Europe/Zurich");
        existing.setOfferingTitleSnapshot(offering.getTitle());
        existing.setDurationSnapshotMinutes(60);
        existing.setIdempotencyKey("idem-2");

        when(businessBookingRepository.findByCustomerUserIdAndIdempotencyKey(customer.getId(), "idem-2"))
                .thenReturn(Optional.of(existing));
        when(businessBookingPresentationService.enrichForCustomer(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = businessCreateBookingUseCase.createCustomerBooking(
                request(offering.getId(), existing.getStartsAt(), existing.getEndsAt(), "idem-2"),
                customer
        );

        assertEquals(9L, result.getId());
        verify(businessBookingPrimitiveService, never()).lockOffering(any());
        verify(businessBookingRepository, never()).save(any());
    }

    private BusinessBookingRequestDTO request(Long offeringId, Instant startsAt, Instant endsAt, String idempotencyKey) {
        BusinessBookingRequestDTO dto = new BusinessBookingRequestDTO();
        dto.setBusinessOfferingId(offeringId);
        dto.setStartsAt(startsAt);
        dto.setEndsAt(endsAt);
        dto.setIdempotencyKey(idempotencyKey);
        dto.setCustomerNote("note");
        return dto;
    }

    private BusinessBookingPolicy policy() {
        BusinessBookingPolicy policy = new BusinessBookingPolicy();
        policy.setLeadTimeMinutes(0);
        policy.setMaxAdvanceDays(30);
        policy.setCustomerCancellationWindowMinutes(1440);
        policy.setOwnerRescheduleWindowMinutes(1440);
        return policy;
    }

    private BusinessOffering offering(AppUser owner, BusinessOfferingBookingMode bookingMode) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(3L);
        profile.setOwner(owner);
        profile.setBusinessName("Dog Groomer");
        profile.setSlug("dog-groomer");
        profile.setTimezone("Europe/Zurich");
        profile.setBookingEnabled(true);
        profile.setActive(true);

        BusinessOffering offering = new BusinessOffering();
        offering.setId(5L);
        offering.setBusinessProfile(profile);
        offering.setTitle("Trim");
        offering.setSlug("trim");
        offering.setBasePriceAmount(new BigDecimal("25.00"));
        offering.setBasePriceCurrency("EUR");
        offering.setBookingMode(bookingMode);
        offering.setActive(true);
        offering.setDefaultDurationMinutes(60);
        offering.setSlotCapacity(1);
        return offering;
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded");
        return user;
    }
}
