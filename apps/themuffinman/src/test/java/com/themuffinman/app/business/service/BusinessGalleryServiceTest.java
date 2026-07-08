package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessGalleryImageRequestDTO;
import com.themuffinman.app.business.mapper.BusinessGalleryImageMgr;
import com.themuffinman.app.business.model.BusinessGalleryImage;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessGalleryImageRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessGalleryServiceTest {

    @Mock
    private BusinessGalleryImageRepository businessGalleryImageRepository;

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Spy
    private BusinessGalleryImageMgr businessGalleryImageMgr = new BusinessGalleryImageMgr();

    @InjectMocks
    private BusinessGalleryService businessGalleryService;

    @Test
    void createMyGalleryImageTrimsValuesAndPersistsMetadata() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(owner);

        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(profile));
        when(businessGalleryImageRepository.save(any(BusinessGalleryImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BusinessGalleryImageRequestDTO dto = new BusinessGalleryImageRequestDTO();
        dto.setImageUrl("  https://cdn.example.com/image.jpg  ");
        dto.setAltText("  Cute dog  ");
        dto.setSortOrder(4);
        dto.setActive(true);

        var result = businessGalleryService.createMyGalleryImage(dto, owner);

        assertEquals("https://cdn.example.com/image.jpg", result.getImageUrl());
        assertEquals("Cute dog", result.getAltText());
        assertEquals(4, result.getSortOrder());
    }

    @Test
    void createMyGalleryImageRejectsBlankUrl() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(owner);
        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.of(profile));

        BusinessGalleryImageRequestDTO dto = new BusinessGalleryImageRequestDTO();
        dto.setImageUrl("   ");

        assertThrows(ResponseStatusException.class, () -> businessGalleryService.createMyGalleryImage(dto, owner));
    }

    private BusinessProfile profile(AppUser owner) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(10L);
        profile.setOwner(owner);
        profile.setBusinessName("Dog Groomer");
        profile.setSlug("dog-groomer");
        profile.setTimezone("Europe/Zurich");
        profile.setBookingEnabled(true);
        profile.setActive(true);
        return profile;
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
