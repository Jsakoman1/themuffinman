package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessFavoriteResponseDTO;
import com.themuffinman.app.business.model.BusinessFavorite;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessFavoriteRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessFavoriteService {
    private final BusinessFavoriteRepository favoriteRepository;
    private final BusinessProfileRepository profileRepository;

    public List<BusinessFavoriteResponseDTO> getMine(AppUser currentUser) {
        return favoriteRepository.findByOwnerIdAndBusinessProfileActiveTrueOrderByCreatedAtDesc(currentUser.getId())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public BusinessFavoriteResponseDTO add(AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = profileRepository.findById(businessProfileId)
                .filter(BusinessProfile::isActive)
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
        BusinessFavorite favorite = favoriteRepository.findByOwnerIdAndBusinessProfileId(currentUser.getId(), businessProfileId)
                .orElseGet(() -> {
                    BusinessFavorite created = new BusinessFavorite();
                    created.setOwner(currentUser);
                    created.setBusinessProfile(profile);
                    return favoriteRepository.save(created);
                });
        return toDto(favorite);
    }

    @Transactional
    public void remove(AppUser currentUser, Long businessProfileId) {
        favoriteRepository.findByOwnerIdAndBusinessProfileId(currentUser.getId(), businessProfileId)
                .ifPresent(favoriteRepository::delete);
    }

    private BusinessFavoriteResponseDTO toDto(BusinessFavorite favorite) {
        BusinessProfile profile = favorite.getBusinessProfile();
        return BusinessFavoriteResponseDTO.builder()
                .id(favorite.getId())
                .businessProfileId(profile.getId())
                .businessName(profile.getBusinessName())
                .slug(profile.getSlug())
                .bookingEnabled(profile.isBookingEnabled())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
