package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessAvailabilityWindowDTO;
import com.themuffinman.app.business.model.BusinessBookingPolicy;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessOfferingDurationMode;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessAvailabilityExceptionRepository;
import com.themuffinman.app.business.repository.BusinessAvailabilityRuleRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessBookingValidationServiceTest {

    @Mock
    private BusinessAvailabilityRuleRepository businessAvailabilityRuleRepository;

    @Mock
    private BusinessAvailabilityExceptionRepository businessAvailabilityExceptionRepository;

    @Mock
    private BusinessAvailabilityComputationService businessAvailabilityComputationService;

    @Mock
    private BusinessBookingPrimitiveService businessBookingPrimitiveService;

    @InjectMocks
    private BusinessBookingValidationService businessBookingValidationService;

    @Test
    void validateCreateRejectsSelfBooking() {
        AppUser owner = user(1L, "owner");
        BusinessOffering offering = offering(owner);

        assertThrows(ResponseStatusException.class, () -> businessBookingValidationService.validateCreate(
                offering,
                owner,
                Instant.parse("2026-07-12T09:00:00Z"),
                Instant.parse("2026-07-12T10:00:00Z"),
                policy()
        ));
    }

    @Test
    void validateCreateRejectsFullCapacity() {
        AppUser owner = user(1L, "owner");
        AppUser customer = user(2L, "customer");
        BusinessOffering offering = offering(owner);
        Instant start = Instant.parse("2026-07-12T09:00:00Z");
        Instant end = Instant.parse("2026-07-12T10:00:00Z");

        when(businessAvailabilityRuleRepository.findActiveByBusinessProfileId(offering.getBusinessProfile().getId())).thenReturn(List.of());
        when(businessAvailabilityExceptionRepository.findByBusinessProfileId(offering.getBusinessProfile().getId())).thenReturn(List.of());
        when(businessAvailabilityComputationService.deriveWindows(
                offering.getBusinessProfile(),
                offering,
                List.of(),
                List.of(),
                start,
                end
        )).thenReturn(List.of(BusinessAvailabilityWindowDTO.builder()
                .startsAt(start)
                .endsAt(end)
                .effectiveCapacity(1)
                .timezone("Europe/Zurich")
                .build()));
        when(businessBookingPrimitiveService.countOverlappingCapacityUsage(offering.getId(), start, end)).thenReturn(1L);

        assertThrows(ResponseStatusException.class, () -> businessBookingValidationService.validateCreate(
                offering,
                customer,
                start,
                end,
                policy()
        ));
    }

    private BusinessBookingPolicy policy() {
        BusinessBookingPolicy policy = new BusinessBookingPolicy();
        policy.setLeadTimeMinutes(0);
        policy.setMaxAdvanceDays(60);
        policy.setCustomerCancellationWindowMinutes(1440);
        policy.setOwnerRescheduleWindowMinutes(1440);
        return policy;
    }

    private BusinessOffering offering(AppUser owner) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L);
        profile.setOwner(owner);
        profile.setBusinessName("Dog Groomer");
        profile.setSlug("dog-groomer");
        profile.setTimezone("Europe/Zurich");
        profile.setBookingEnabled(true);
        profile.setActive(true);

        BusinessOffering offering = new BusinessOffering();
        offering.setId(20L);
        offering.setBusinessProfile(profile);
        offering.setTitle("Trim");
        offering.setSlug("trim");
        offering.setActive(true);
        offering.setDurationMode(BusinessOfferingDurationMode.FIXED);
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
