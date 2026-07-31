package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessOfferingSetupServiceTest {
    @Mock private BusinessProfileRepository businessProfileRepository;
    @InjectMocks private BusinessOfferingSetupService businessOfferingSetupService;

    @Test
    void publishesBackendOwnedDefaultsOptionsAndSetupGuidance() {
        AppUser owner = new AppUser(); owner.setId(7L);
        BusinessProfile profile = new BusinessProfile(); profile.setId(9L); profile.setOwner(owner);
        when(businessProfileRepository.findById(9L)).thenReturn(Optional.of(profile));

        var setup = businessOfferingSetupService.getSetup(owner, 9L);

        assertEquals("business-offering-setup-v1", setup.getContractVersion());
        assertEquals("CHF", setup.getDefaults().getBasePriceCurrency());
        assertEquals(60, setup.getDefaults().getDefaultDurationMinutes());
        assertEquals("One fixed price", setup.getPricingTypes().getFirst().getLabel());
        assertEquals("basics", setup.getSteps().getFirst().getId());
        assertTrue(setup.getFields().stream().anyMatch(field -> field.getName().equals("title") && field.isRequired()));
    }
}
