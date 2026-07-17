package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessBookingPresentationServiceTest {

    @Mock
    private BusinessBookingPolicyService businessBookingPolicyService;

    @InjectMocks
    private BusinessBookingPresentationService businessBookingPresentationService;

    @Test
    void enrichForCustomerUsesReadOnlyPolicyResolutionAndBackendOwnedActions() {
        AppUser owner = user(1L, "owner");
        AppUser customer = user(2L, "customer");
        BusinessBooking booking = booking(owner, customer);
        BusinessBookingPolicy policy = new BusinessBookingPolicy();
        policy.setAllowCustomerCancellation(true);
        policy.setCustomerCancellationWindowMinutes(0);

        when(businessBookingPolicyService.resolveEffectivePolicy(owner)).thenReturn(policy);

        var result = businessBookingPresentationService.enrichForCustomer(
                com.themuffinman.app.business.dto.BusinessBookingResponseDTO.builder().build(),
                booking,
                customer
        );

        assertEquals("Pending confirmation", result.getStatusLabel());
        assertEquals(2, result.getAllowedActions().size());
        assertEquals("CANCEL", result.getAllowedActions().getFirst().name());
        verify(businessBookingPolicyService).resolveEffectivePolicy(owner);
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
        booking.setStatus(BusinessBookingStatus.PENDING_CONFIRMATION);
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
