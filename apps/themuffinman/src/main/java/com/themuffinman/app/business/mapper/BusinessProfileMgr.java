package com.themuffinman.app.business.mapper;

import com.themuffinman.app.business.dto.BusinessProfileResponseDTO;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import org.springframework.stereotype.Component;

@Component
public class BusinessProfileMgr {

    public BusinessProfileResponseDTO toDto(BusinessProfile profile) {
        if (profile == null) {
            return null;
        }

        return BusinessProfileResponseDTO.builder()
                .id(profile.getId())
                .ownerId(profile.getOwner().getId())
                .ownerUsername(profile.getOwner().getUsername())
                .businessName(profile.getBusinessName())
                .slug(profile.getSlug())
                .headline(profile.getHeadline())
                .description(RichTextInputValidator.sanitize(profile.getDescription()))
                .contactEmail(profile.getContactEmail())
                .contactPhone(profile.getContactPhone())
                .websiteUrl(profile.getWebsiteUrl())
                .active(profile.isActive())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
