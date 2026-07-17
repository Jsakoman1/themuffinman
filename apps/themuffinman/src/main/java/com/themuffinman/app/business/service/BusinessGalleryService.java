package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessGalleryImageListResponseDTO;
import com.themuffinman.app.business.dto.BusinessGalleryImageRequestDTO;
import com.themuffinman.app.business.dto.BusinessGalleryImageResponseDTO;
import com.themuffinman.app.business.mapper.BusinessGalleryImageMgr;
import com.themuffinman.app.business.model.BusinessGalleryImage;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessGalleryImageRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessGalleryService {

    private final BusinessGalleryImageRepository businessGalleryImageRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessGalleryImageMgr businessGalleryImageMgr;

    public BusinessGalleryImageListResponseDTO getMyGallery(AppUser currentUser) {
        return getMyGallery(currentUser, null);
    }

    public BusinessGalleryImageListResponseDTO getMyGallery(AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = businessProfileId == null ? null : requireOwnerProfile(currentUser, businessProfileId);
        return BusinessGalleryImageListResponseDTO.builder()
                .items((profile == null ? businessGalleryImageRepository.findByOwnerId(currentUser.getId()) : businessGalleryImageRepository.findByBusinessProfileId(profile.getId(), currentUser.getId())).stream()
                        .map(businessGalleryImageMgr::toDto)
                        .toList())
                .build();
    }

    public BusinessGalleryImageListResponseDTO getPublicGallery(Long businessProfileId) {
        return BusinessGalleryImageListResponseDTO.builder()
                .items(businessGalleryImageRepository.findActiveByBusinessProfileId(businessProfileId).stream()
                        .map(businessGalleryImageMgr::toDto)
                        .toList())
                .build();
    }

    @Transactional
    public BusinessGalleryImageResponseDTO createMyGalleryImage(BusinessGalleryImageRequestDTO dto, AppUser currentUser) {
        return createMyGalleryImage(dto, currentUser, null);
    }

    @Transactional
    public BusinessGalleryImageResponseDTO createMyGalleryImage(BusinessGalleryImageRequestDTO dto, AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = businessProfileId == null ? businessProfileRepository.findByOwnerId(currentUser.getId()).orElseThrow(() -> ServiceErrors.badRequest("Create your business profile before managing gallery")) : requireOwnerProfile(currentUser, businessProfileId);
        BusinessGalleryImage image = new BusinessGalleryImage();
        image.setBusinessProfile(profile);
        apply(image, dto);
        return businessGalleryImageMgr.toDto(businessGalleryImageRepository.save(image));
    }

    @Transactional
    public BusinessGalleryImageResponseDTO updateMyGalleryImage(Long imageId, BusinessGalleryImageRequestDTO dto, AppUser currentUser) {
        BusinessGalleryImage image = businessGalleryImageRepository.findOwnedById(imageId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Gallery image not found"));
        apply(image, dto);
        return businessGalleryImageMgr.toDto(businessGalleryImageRepository.save(image));
    }

    @Transactional
    public void deleteMyGalleryImage(Long imageId, AppUser currentUser) {
        BusinessGalleryImage image = businessGalleryImageRepository.findOwnedById(imageId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Gallery image not found"));
        businessGalleryImageRepository.delete(image);
    }

    private void apply(BusinessGalleryImage image, BusinessGalleryImageRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Gallery image request is required");
        }
        String imageUrl = dto.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            throw ServiceErrors.badRequest("Image url is required");
        }
        image.setImageUrl(imageUrl.trim());
        image.setAltText(dto.getAltText() == null ? null : dto.getAltText().trim());
        image.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        image.setActive(dto.getActive() == null || dto.getActive());
    }

    private BusinessProfile requireOwnerProfile(AppUser currentUser, Long businessProfileId) {
        return businessProfileRepository.findById(businessProfileId)
                .filter(profile -> profile.getOwner().getId().equals(currentUser.getId()))
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
    }
}
