package com.themuffinman.app.identity.mapper;

import com.themuffinman.app.identity.dto.ProfileGalleryImageResponseDTO;
import com.themuffinman.app.identity.model.ProfileGalleryImage;
import org.springframework.stereotype.Component;

@Component
public class ProfileGalleryImageMgr {
    public ProfileGalleryImageResponseDTO toDto(ProfileGalleryImage image) {
        return ProfileGalleryImageResponseDTO.builder()
                .id(image.getId())
                .ownerId(image.getOwner().getId())
                .imageUrl(image.getImageUrl())
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .active(image.isActive())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
