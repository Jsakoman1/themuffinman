package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessProfileRequestDTO;
import com.themuffinman.app.business.mapper.BusinessProfileMgr;
import com.themuffinman.app.business.model.BusinessProfile;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessProfileServiceTest {

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @Spy
    private BusinessProfileMgr businessProfileMgr = new BusinessProfileMgr();

    @InjectMocks
    private BusinessProfileService businessProfileService;

    @Test
    void saveMyProfileCreatesProfileWithDerivedSlug() {
        AppUser owner = user(1L, "owner");

        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.empty());
        when(businessProfileRepository.existsBySlug("blue-bakery")).thenReturn(false);
        when(businessProfileRepository.save(any(BusinessProfile.class))).thenAnswer(invocation -> {
            BusinessProfile profile = invocation.getArgument(0);
            profile.setId(11L);
            return profile;
        });

        var result = businessProfileService.saveMyProfile(BusinessProfileRequestDTO.builder()
                .businessName("Blue Bakery")
                .headline("Morning bread")
                .description("<p>Fresh</p>")
                .timezone("Europe/Zurich")
                .bookingEnabled(true)
                .publicAddressLabel("Main Square 1")
                .contactWhatsapp("+385123456")
                .active(true)
                .build(), owner);

        assertEquals(11L, result.getId());
        assertEquals("Blue Bakery", result.getBusinessName());
        assertEquals("blue-bakery", result.getSlug());
        assertEquals(owner.getUsername(), result.getOwnerUsername());
        assertEquals("Europe/Zurich", result.getTimezone());
        assertEquals("Main Square 1", result.getPublicAddressLabel());
        assertEquals("+385123456", result.getContactWhatsapp());
    }

    @Test
    void saveMyProfileRejectsDuplicateSlug() {
        AppUser owner = user(1L, "owner");

        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.empty());
        when(businessProfileRepository.existsBySlug("blue-bakery")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> businessProfileService.saveMyProfile(BusinessProfileRequestDTO.builder()
                .businessName("Blue Bakery")
                .slug("blue-bakery")
                .build(), owner));
    }

    @Test
    void getDirectoryReturnsActiveProfiles() {
        BusinessProfile active = profile(11L, user(1L, "owner"), "Blue Bakery", "blue-bakery", true);

        when(businessProfileRepository.findActiveProfiles()).thenReturn(List.of(active));

        var result = businessProfileService.getDirectory("");

        assertEquals(1, result.getItems().size());
        assertEquals("blue-bakery", result.getItems().getFirst().getSlug());
    }

    @Test
    void getDirectoryUsesBackendSearchWhenQueryIsPresent() {
        BusinessProfile active = profile(11L, user(1L, "owner"), "Blue Bakery", "blue-bakery", true);

        when(businessProfileRepository.searchActiveProfiles("bakery")).thenReturn(List.of(active));

        var result = businessProfileService.getDirectory(" bakery ");

        assertEquals(List.of("blue-bakery"), result.getItems().stream().map(item -> item.getSlug()).toList());
    }

    @Test
    void getProfileBySlugHidesInactiveProfiles() {
        BusinessProfile inactive = profile(11L, user(1L, "owner"), "Blue Bakery", "blue-bakery", false);

        when(businessProfileRepository.findBySlug("blue-bakery")).thenReturn(Optional.of(inactive));

        assertThrows(ResponseStatusException.class, () -> businessProfileService.getProfileBySlug("blue-bakery"));
    }

    @Test
    void getMyProfileReturnsNullWhenMissing() {
        AppUser owner = user(1L, "owner");

        when(businessProfileRepository.findByOwnerId(owner.getId())).thenReturn(Optional.empty());

        assertNull(businessProfileService.getMyProfile(owner));
    }

    @Test
    void archiveMyProfileIsOwnerScopedAndHidesItFromPublicDirectory() {
        AppUser owner = user(1L, "owner");
        BusinessProfile profile = profile(11L, owner, "Blue Bakery", "blue-bakery", true);
        when(businessProfileRepository.findById(11L)).thenReturn(Optional.of(profile));
        when(businessProfileRepository.save(profile)).thenReturn(profile);

        var result = businessProfileService.archiveMyProfile(11L, owner);

        assertEquals(false, result.isActive());
        verify(businessProfileRepository).save(profile);
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded");
        return user;
    }

    private BusinessProfile profile(Long id, AppUser owner, String name, String slug, boolean active) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(id);
        profile.setOwner(owner);
        profile.setBusinessName(name);
        profile.setSlug(slug);
        profile.setActive(active);
        return profile;
    }
}
