package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessOfferingRequestDTO;
import com.themuffinman.app.business.mapper.BusinessOfferingMgr;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessOfferingBookingMode;
import com.themuffinman.app.business.model.BusinessOfferingPricingType;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessOfferingServiceTest {

    @Mock
    private BusinessOfferingRepository businessOfferingRepository;

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Spy
    private BusinessOfferingMgr businessOfferingMgr = new BusinessOfferingMgr();

    @InjectMocks
    private BusinessOfferingService businessOfferingService;

    @Test
    void createMyOfferingCreatesOfferingWithDerivedSlug() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(5L, owner, "Blue Bakery");

        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(profile));
        when(businessOfferingRepository.existsByBusinessProfileIdAndSlug(profile.getId(), "full-grooming")).thenReturn(false);
        when(businessOfferingRepository.save(any(BusinessOffering.class))).thenAnswer(invocation -> {
            BusinessOffering offering = invocation.getArgument(0);
            offering.setId(9L);
            return offering;
        });

        var result = businessOfferingService.createMyOffering(BusinessOfferingRequestDTO.builder()
                .title("Full Grooming")
                .pricingType(BusinessOfferingPricingType.FIXED)
                .basePriceAmount(new BigDecimal("40.00"))
                .basePriceCurrency("EUR")
                .defaultDurationMinutes(60)
                .slotCapacity(1)
                .bookingMode(BusinessOfferingBookingMode.INSTANT)
                .build(), owner);

        assertEquals(9L, result.getId());
        assertEquals("full-grooming", result.getSlug());
        assertEquals(BusinessOfferingBookingMode.INSTANT, result.getBookingMode());
        assertEquals(false, result.isRequiresOwnerConfirmation());
    }

    @Test
    void createMyOfferingRejectsDuplicateSlug() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(5L, owner, "Blue Bakery");

        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(profile));
        when(businessOfferingRepository.existsByBusinessProfileIdAndSlug(profile.getId(), "full-grooming")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> businessOfferingService.createMyOffering(
                BusinessOfferingRequestDTO.builder()
                        .title("Full Grooming")
                        .slug("full-grooming")
                        .pricingType(BusinessOfferingPricingType.FIXED)
                        .basePriceAmount(new BigDecimal("40.00"))
                        .basePriceCurrency("EUR")
                        .defaultDurationMinutes(60)
                        .slotCapacity(1)
                        .build(),
                owner
        ));
    }

    @Test
    void getMyOfferingsReturnsOwnerItems() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(5L, owner, "Blue Bakery");
        BusinessOffering offering = new BusinessOffering();
        offering.setId(7L);
        offering.setBusinessProfile(profile);
        offering.setTitle("Full Grooming");
        offering.setSlug("full-grooming");

        when(businessOfferingRepository.findByOwnerId(owner.getId())).thenReturn(List.of(offering));

        var result = businessOfferingService.getMyOfferings(owner);

        assertEquals(1, result.getItems().size());
        assertEquals("full-grooming", result.getItems().getFirst().getSlug());
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded");
        return user;
    }

    private BusinessProfile profile(Long id, AppUser owner, String businessName) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(id);
        profile.setOwner(owner);
        profile.setBusinessName(businessName);
        profile.setSlug("blue-bakery");
        return profile;
    }
}
