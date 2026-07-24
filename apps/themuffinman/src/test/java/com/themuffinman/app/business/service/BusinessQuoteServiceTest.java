package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessQuoteRequestDTO;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessOfferingPricingType;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessQuoteServiceTest {
    @Mock
    private BusinessOfferingRepository offeringRepository;
    @Mock
    private BusinessPricingCalculationService pricingService;
    @InjectMocks
    private BusinessQuoteService service;

    @Test
    void acceptsOnlyActiveBookingEnabledPublicOffering() {
        BusinessProfile profile = new BusinessProfile();
        profile.setSlug("wash");
        profile.setActive(true);
        profile.setBookingEnabled(true);
        BusinessOffering offering = new BusinessOffering();
        offering.setId(4L);
        offering.setTitle("Wash");
        offering.setActive(true);
        offering.setBusinessProfile(profile);
        offering.setPricingType(BusinessOfferingPricingType.FIXED);

        BusinessQuoteRequestDTO request = new BusinessQuoteRequestDTO();
        request.setBusinessOfferingId(4L);
        when(offeringRepository.findById(4L)).thenReturn(Optional.of(offering));
        when(pricingService.calculate(offering, request)).thenReturn(
                com.themuffinman.app.business.dto.BusinessQuoteResponseDTO.builder()
                        .businessOfferingId(4L).totalAmount(new BigDecimal("20.00")).build());

        assertEquals(new BigDecimal("20.00"), service.quote("wash", request).getTotalAmount());
    }
}
