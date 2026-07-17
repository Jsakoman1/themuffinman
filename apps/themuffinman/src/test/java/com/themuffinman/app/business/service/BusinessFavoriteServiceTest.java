package com.themuffinman.app.business.service;

import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.model.BusinessFavorite;
import com.themuffinman.app.business.repository.BusinessFavoriteRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessFavoriteServiceTest {
    @Mock
    private BusinessFavoriteRepository favoriteRepository;

    @Mock
    private BusinessProfileRepository profileRepository;

    @InjectMocks
    private BusinessFavoriteService service;

    @Test
    void addIsIdempotentForSameBusiness() {
        AppUser user = user(1L);
        BusinessProfile profile = profile(10L, user);
        BusinessFavorite favorite = favorite(user, profile);
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(favoriteRepository.findByOwnerIdAndBusinessProfileId(1L, 10L)).thenReturn(Optional.of(favorite));

        var result = service.add(user, 10L);

        assertEquals("Dog Groomer", result.getBusinessName());
        verify(favoriteRepository).findByOwnerIdAndBusinessProfileId(1L, 10L);
    }

    @Test
    void addCreatesFavoriteForVisibleBusiness() {
        AppUser user = user(1L);
        BusinessProfile profile = profile(10L, user);
        when(profileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(favoriteRepository.findByOwnerIdAndBusinessProfileId(1L, 10L)).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(BusinessFavorite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.add(user, 10L);

        assertEquals(10L, result.getBusinessProfileId());
        assertEquals("dog-groomer", result.getSlug());
    }

    @Test
    void getMineUsesActiveBusinessProjection() {
        AppUser user = user(1L);
        BusinessProfile profile = profile(10L, user);
        when(favoriteRepository.findByOwnerIdAndBusinessProfileActiveTrueOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(favorite(user, profile)));

        var result = service.getMine(user);

        assertEquals(List.of("dog-groomer"), result.stream().map(item -> item.getSlug()).toList());
        verify(favoriteRepository).findByOwnerIdAndBusinessProfileActiveTrueOrderByCreatedAtDesc(1L);
    }

    private BusinessFavorite favorite(AppUser user, BusinessProfile profile) {
        BusinessFavorite favorite = new BusinessFavorite();
        favorite.setId(20L);
        favorite.setOwner(user);
        favorite.setBusinessProfile(profile);
        return favorite;
    }

    private BusinessProfile profile(Long id, AppUser owner) {
        BusinessProfile profile = new BusinessProfile();
        profile.setId(id);
        profile.setOwner(owner);
        profile.setBusinessName("Dog Groomer");
        profile.setSlug("dog-groomer");
        profile.setBookingEnabled(true);
        profile.setActive(true);
        return profile;
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("owner");
        user.setEmail("owner@example.com");
        user.setPasswordHash("encoded");
        return user;
    }
}
