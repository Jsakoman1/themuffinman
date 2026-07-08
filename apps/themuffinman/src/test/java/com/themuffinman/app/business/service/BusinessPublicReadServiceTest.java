package com.themuffinman.app.business.service;

import com.themuffinman.app.business.mapper.BusinessOfferingMgr;
import com.themuffinman.app.business.mapper.BusinessGalleryImageMgr;
import com.themuffinman.app.business.repository.BusinessGalleryImageRepository;
import com.themuffinman.app.business.model.BusinessOffering;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessPublicReadServiceTest {

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Mock
    private BusinessOfferingRepository businessOfferingRepository;

    @Mock
    private BusinessGalleryImageRepository businessGalleryImageRepository;

    @Spy
    private BusinessOfferingMgr businessOfferingMgr = new BusinessOfferingMgr();

    @Spy
    private BusinessGalleryImageMgr businessGalleryImageMgr = new BusinessGalleryImageMgr();

    @InjectMocks
    private BusinessPublicReadService businessPublicReadService;

    @Test
    void getPublicBusinessPageReturnsActiveProfileWithOfferings() {
        AppUser owner = new AppUser();
        owner.setId(1L);
        owner.setUsername("owner");

        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L);
        profile.setOwner(owner);
        profile.setBusinessName("Dog Place");
        profile.setSlug("dog-place");
        profile.setActive(true);
        profile.setBookingEnabled(true);
        profile.setTimezone("Europe/Zurich");

        BusinessOffering offering = new BusinessOffering();
        offering.setId(7L);
        offering.setBusinessProfile(profile);
        offering.setTitle("Full Grooming");
        offering.setSlug("full-grooming");

        when(businessProfileRepository.findBySlug("dog-place")).thenReturn(Optional.of(profile));
        when(businessOfferingRepository.findActiveByBusinessProfileId(profile.getId())).thenReturn(List.of(offering));
        when(businessGalleryImageRepository.findActiveByBusinessProfileId(profile.getId())).thenReturn(List.of());

        var result = businessPublicReadService.getPublicBusinessPage("dog-place");

        assertEquals("dog-place", result.getSlug());
        assertEquals(1, result.getOfferings().size());
        assertEquals("full-grooming", result.getOfferings().getFirst().getSlug());
    }

    @Test
    void getPublicBusinessPageRejectsInactiveProfile() {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L);
        profile.setSlug("dog-place");
        profile.setActive(false);

        when(businessProfileRepository.findBySlug("dog-place")).thenReturn(Optional.of(profile));

        assertThrows(ResponseStatusException.class, () -> businessPublicReadService.getPublicBusinessPage("dog-place"));
    }
}
