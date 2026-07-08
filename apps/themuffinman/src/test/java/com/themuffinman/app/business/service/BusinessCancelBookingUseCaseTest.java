package com.themuffinman.app.business.service;

import com.themuffinman.app.business.mapper.BusinessBookingMgr;
import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessBookingRepository;
import com.themuffinman.app.common.event.DomainEvent;
import com.themuffinman.app.common.event.DomainEventPublisher;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessCancelBookingUseCaseTest {

    @Mock
    private BusinessBookingRepository businessBookingRepository;

    @Mock
    private BusinessBookingPolicyService businessBookingPolicyService;

    @Mock
    private BusinessBookingValidationService businessBookingValidationService;

    @Spy
    private BusinessBookingMgr businessBookingMgr = new BusinessBookingMgr();

    @Mock
    private BusinessBookingPresentationService businessBookingPresentationService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private BusinessCancelBookingUseCase businessCancelBookingUseCase;

    @Test
    void cancelAsCustomerTransitionsBooking() {
        AppUser owner = user(1L, "owner");
        AppUser customer = user(2L, "customer");
        BusinessBooking booking = booking(owner, customer);
        BusinessBookingPolicy policy = new BusinessBookingPolicy();
        policy.setCustomerCancellationWindowMinutes(1440);

        when(businessBookingRepository.findDetailedByIdAndCustomerUserId(booking.getId(), customer.getId())).thenReturn(Optional.of(booking));
        when(businessBookingPolicyService.loadOrCreatePolicyEntity(owner)).thenReturn(policy);
        when(businessBookingRepository.save(any(BusinessBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(businessBookingPresentationService.enrichForCustomer(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = businessCancelBookingUseCase.cancelAsCustomer(booking.getId(), customer);

        assertEquals(BusinessBookingStatus.CANCELLED_BY_CUSTOMER, result.getStatus());
        verify(domainEventPublisher).publish(any(DomainEvent.class));
    }

    private BusinessBooking booking(AppUser owner, AppUser customer) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(3L);
        profile.setOwner(owner);
        profile.setBusinessName("Dog Groomer");
        profile.setSlug("dog-groomer");
        profile.setTimezone("Europe/Zurich");
        profile.setBookingEnabled(true);
        profile.setActive(true);

        BusinessOffering offering = new BusinessOffering();
        offering.setId(4L);
        offering.setBusinessProfile(profile);
        offering.setTitle("Trim");
        offering.setSlug("trim");

        BusinessBooking booking = new BusinessBooking();
        booking.setId(5L);
        booking.setBusinessProfile(profile);
        booking.setBusinessOffering(offering);
        booking.setCustomerUser(customer);
        booking.setStatus(BusinessBookingStatus.CONFIRMED);
        booking.setStartsAt(Instant.now().plusSeconds(7200));
        booking.setEndsAt(Instant.now().plusSeconds(10800));
        booking.setTimezone("Europe/Zurich");
        booking.setOfferingTitleSnapshot("Trim");
        booking.setDurationSnapshotMinutes(60);
        return booking;
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
