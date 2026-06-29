package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessProfileListResponseDTO;
import com.themuffinman.app.business.dto.BusinessProfileRequestDTO;
import com.themuffinman.app.business.dto.BusinessProfileResponseDTO;
import com.themuffinman.app.business.mapper.BusinessProfileMgr;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

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
        BusinessProfile profile = businessProfileRepository.findBySlug(normalizeSlug(slug))
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

        String businessName = normalizeRequired(dto.getBusinessName(), "Business name is required");
        String slug = dto.getSlug() == null || dto.getSlug().isBlank()
                ? slugify(businessName)
                : normalizeSlug(dto.getSlug());

        validateSlugAvailable(slug, currentUser.getId(), profile.getId() == null);

        profile.setBusinessName(businessName);
        profile.setSlug(slug);
        profile.setHeadline(normalizeOptional(dto.getHeadline()));
        profile.setDescription(RichTextInputValidator.sanitize(dto.getDescription()));
        profile.setContactEmail(normalizeOptional(dto.getContactEmail()));
        profile.setContactPhone(normalizeOptional(dto.getContactPhone()));
        profile.setWebsiteUrl(normalizeOptional(dto.getWebsiteUrl()));
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

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw ServiceErrors.badRequest(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeSlug(String value) {
        String normalized = normalizeRequired(value, "Business slug is required").toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
            throw ServiceErrors.badRequest("Business slug must use lowercase letters, numbers, and hyphens");
        }
        return normalized;
    }

    private String slugify(String value) {
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isBlank()) {
            throw ServiceErrors.badRequest("Business slug could not be derived from the business name");
        }
        return slug;
    }
}
