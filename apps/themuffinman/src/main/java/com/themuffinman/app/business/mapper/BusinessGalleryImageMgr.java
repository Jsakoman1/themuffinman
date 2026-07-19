package com.themuffinman.app.business.mapper;

import com.themuffinman.app.business.dto.BusinessGalleryImageResponseDTO;
import com.themuffinman.app.business.model.BusinessGalleryImage;
import org.springframework.stereotype.Component;

@Component
public class BusinessGalleryImageMgr {

    public BusinessGalleryImageResponseDTO toDto(BusinessGalleryImage image) {
        if (image == null) {
            return null;
        }

        return BusinessGalleryImageResponseDTO.builder()
                .id(image.getId())
                .businessProfileId(image.getBusinessProfile().getId())
                .imageUrl(image.getImageUrl())
                .storageProvider(image.getStorageProvider())
                .storageKey(image.getStorageKey())
                .contentType(image.getContentType())
                .availability("AVAILABLE")
                .altText(image.getAltText())
                .sortOrder(image.getSortOrder())
                .active(image.isActive())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
