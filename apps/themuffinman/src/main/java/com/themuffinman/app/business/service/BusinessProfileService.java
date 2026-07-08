package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessProfileListResponseDTO;
import com.themuffinman.app.business.dto.BusinessProfileRequestDTO;
import com.themuffinman.app.business.dto.BusinessProfileResponseDTO;
import com.themuffinman.app.business.mapper.BusinessProfileMgr;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.SlugNormalizer;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessProfileMgr businessProfileMgr;

    public BusinessProfileListResponseDTO getDirectory() {
        return BusinessProfileListResponseDTO.builder()
                .items(businessProfileRepository.findActiveProfiles().stream()
                        .map(businessProfileMgr::toDto)
                        .toList())
                .build();
    }

    public BusinessProfileResponseDTO getProfileBySlug(String slug) {
        BusinessProfile profile = businessProfileRepository.findBySlug(
                        SlugNormalizer.normalizeSlug(
                                slug,
                                "Business slug is required",
                                "Business slug must use lowercase letters, numbers, and hyphens"
                        ))
                .filter(BusinessProfile::isActive)
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
        return businessProfileMgr.toDto(profile);
    }

    public BusinessProfileResponseDTO getMyProfile(AppUser currentUser) {
        return businessProfileRepository.findByOwnerId(currentUser.getId())
                .map(businessProfileMgr::toDto)
                .orElse(null);
    }

    @Transactional
    public BusinessProfileResponseDTO saveMyProfile(BusinessProfileRequestDTO dto, AppUser currentUser) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Business profile request is required");
        }

        BusinessProfile profile = businessProfileRepository.findByOwnerId(currentUser.getId())
                .orElseGet(() -> {
                    BusinessProfile created = new BusinessProfile();
                    created.setOwner(currentUser);
                    return created;
                });

        String businessName = TextValueNormalizer.requireTrimmed(dto.getBusinessName(), "Business name is required");
        String slug = dto.getSlug() == null || dto.getSlug().isBlank()
                ? SlugNormalizer.slugify(businessName, "Business slug could not be derived from the business name")
                : SlugNormalizer.normalizeSlug(dto.getSlug(), "Business slug is required", "Business slug must use lowercase letters, numbers, and hyphens");

        validateSlugAvailable(slug, currentUser.getId(), profile.getId() == null);

        profile.setBusinessName(businessName);
        profile.setSlug(slug);
        profile.setHeadline(TextValueNormalizer.trimToNull(dto.getHeadline()));
        profile.setDescription(RichTextInputValidator.sanitize(dto.getDescription()));
        profile.setContactEmail(TextValueNormalizer.trimToNull(dto.getContactEmail()));
        profile.setContactPhone(TextValueNormalizer.trimToNull(dto.getContactPhone()));
        profile.setWebsiteUrl(TextValueNormalizer.trimToNull(dto.getWebsiteUrl()));
        profile.setTimezone(TextValueNormalizer.trimToNull(dto.getTimezone()));
        profile.setBookingEnabled(dto.getBookingEnabled() != null && dto.getBookingEnabled());
        profile.setPublicAddressLabel(TextValueNormalizer.trimToNull(dto.getPublicAddressLabel()));
        profile.setLatitude(dto.getLatitude());
        profile.setLongitude(dto.getLongitude());
        profile.setContactWhatsapp(TextValueNormalizer.trimToNull(dto.getContactWhatsapp()));
        profile.setHeroImageUrl(TextValueNormalizer.trimToNull(dto.getHeroImageUrl()));
        profile.setActive(dto.getActive() == null || dto.getActive());

        return businessProfileMgr.toDto(businessProfileRepository.save(profile));
    }

    private void validateSlugAvailable(String slug, Long ownerId, boolean creating) {
        boolean taken = creating
                ? businessProfileRepository.existsBySlug(slug)
                : businessProfileRepository.existsBySlugAndOwnerIdNot(slug, ownerId);
        if (taken) {
            throw ServiceErrors.conflict("Business slug is already in use");
        }
    }

}
