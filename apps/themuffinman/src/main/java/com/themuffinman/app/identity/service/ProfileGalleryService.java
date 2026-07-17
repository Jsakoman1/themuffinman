package com.themuffinman.app.identity.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.dto.ProfileGalleryImageListResponseDTO;
import com.themuffinman.app.identity.dto.ProfileGalleryImageRequestDTO;
import com.themuffinman.app.identity.dto.ProfileGalleryImageResponseDTO;
import com.themuffinman.app.identity.mapper.ProfileGalleryImageMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.ProfileGalleryImage;
import com.themuffinman.app.identity.repository.ProfileGalleryImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileGalleryService {
    private final ProfileGalleryImageRepository repository;
    private final ProfileGalleryImageMgr mapper;

    public ProfileGalleryImageListResponseDTO getMyGallery(AppUser currentUser) {
        return ProfileGalleryImageListResponseDTO.builder()
                .items(repository.findByOwnerIdAndActiveTrueOrderBySortOrderAscIdAsc(currentUser.getId()).stream().map(mapper::toDto).toList())
                .build();
    }

    @Transactional
    public ProfileGalleryImageResponseDTO create(AppUser currentUser, ProfileGalleryImageRequestDTO dto) {
        ProfileGalleryImage image = new ProfileGalleryImage();
        image.setOwner(currentUser);
        apply(image, dto);
        return mapper.toDto(repository.save(image));
    }

    @Transactional
    public ProfileGalleryImageResponseDTO update(AppUser currentUser, Long imageId, ProfileGalleryImageRequestDTO dto) {
        ProfileGalleryImage image = repository.findOwnedById(imageId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Profile gallery image not found"));
        apply(image, dto);
        return mapper.toDto(repository.save(image));
    }

    @Transactional
    public void delete(AppUser currentUser, Long imageId) {
        ProfileGalleryImage image = repository.findOwnedById(imageId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Profile gallery image not found"));
        repository.delete(image);
    }

    private void apply(ProfileGalleryImage image, ProfileGalleryImageRequestDTO dto) {
        if (dto == null || dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
            throw ServiceErrors.badRequest("Image url is required");
        }
        image.setImageUrl(dto.getImageUrl().trim());
        image.setAltText(dto.getAltText() == null ? null : dto.getAltText().trim());
        image.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        image.setActive(dto.getActive() == null || dto.getActive());
    }
}
