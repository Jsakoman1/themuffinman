package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessQuoteRequestDTO;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessOfferingPricingType;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.model.BusinessPricingRule;
import com.themuffinman.app.business.repository.BusinessPricingRuleRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BusinessPricingCalculationServiceTest {
    @Test
    void calculatesQuantityAndFixedDurationAndExplainsTheResult() {
        BusinessProfile profile = new BusinessProfile();
        profile.setTimezone("Europe/Zurich");
        BusinessOffering offering = new BusinessOffering();
        offering.setId(7L);
        offering.setTitle("Dog daycare");
        offering.setBusinessProfile(profile);
        offering.setPricingType(BusinessOfferingPricingType.FIXED);
        offering.setBasePriceAmount(new BigDecimal("25.00"));
        offering.setBasePriceCurrency("CHF");
        offering.setDefaultDurationMinutes(1440);

        BusinessQuoteRequestDTO request = new BusinessQuoteRequestDTO();
        request.setBusinessOfferingId(7L);
        request.setQuantity(new BigDecimal("3"));

        var result = new BusinessPricingCalculationService().calculate(offering, request);

        assertEquals(new BigDecimal("75.00"), result.getTotalAmount());
        assertEquals(1440, result.getDurationMinutes());
        assertEquals("CHF", result.getCurrency());
        assertEquals("CALCULATED", result.getPricingState());
    }

    @Test
    void appliesPerHourRuleOnTopOfBasePrice() {
        BusinessProfile profile = new BusinessProfile();
        profile.setTimezone("Europe/Zurich");
        BusinessOffering offering = new BusinessOffering();
        offering.setId(8L);
        offering.setTitle("Car wash");
        offering.setBusinessProfile(profile);
        offering.setPricingType(BusinessOfferingPricingType.FIXED);
        offering.setBasePriceAmount(new BigDecimal("20.00"));
        offering.setBasePriceCurrency("CHF");
        offering.setDefaultDurationMinutes(120);
        BusinessPricingRule rule = new BusinessPricingRule();
        rule.setRuleKey("labor");
        rule.setRuleType("ADD_ON");
        rule.setBillingUnit("HOUR");
        rule.setAmount(new BigDecimal("5.00"));
        BusinessPricingRuleRepository repository = mock(BusinessPricingRuleRepository.class);
        when(repository.findByBusinessOfferingIdAndActiveTrueOrderBySortOrderAscIdAsc(8L)).thenReturn(java.util.List.of(rule));

        BusinessQuoteRequestDTO request = new BusinessQuoteRequestDTO();
        request.setBusinessOfferingId(8L);
        var result = new BusinessPricingCalculationService(repository).calculate(offering, request);

        assertEquals(new BigDecimal("30.00"), result.getTotalAmount());
        org.junit.jupiter.api.Assertions.assertTrue(result.getExplanations().stream().anyMatch(item -> item.contains("labor")));
    }
}
