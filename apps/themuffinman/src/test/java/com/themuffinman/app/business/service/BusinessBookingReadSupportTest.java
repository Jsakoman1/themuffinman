package com.themuffinman.app.business.service;

import com.themuffinman.app.config.BusinessProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessBookingReadSupportTest {

    @Test
    void safePageAndSizeApplyBackendDefaults() {
        BusinessProperties properties = new BusinessProperties();
        properties.getLists().setDefaultPageSize(25);
        properties.getLists().setMaxPageSize(50);

        assertEquals(0, BusinessBookingReadSupport.safePage(-1));
        assertEquals(25, BusinessBookingReadSupport.safeSize(null, properties));
        assertEquals(1, BusinessBookingReadSupport.safeSize(1, properties));
        assertEquals(50, BusinessBookingReadSupport.safeSize(500, properties));
    }

    @Test
    void normalizeQueryTrimsAndLowercasesInput() {
        assertEquals("dog groomer", BusinessBookingReadSupport.normalizeQuery("  Dog Groomer  "));
    }
}
