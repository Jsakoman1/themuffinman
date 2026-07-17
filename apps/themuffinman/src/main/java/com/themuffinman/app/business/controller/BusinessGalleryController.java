package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessGalleryImageListResponseDTO;
import com.themuffinman.app.business.dto.BusinessGalleryImageRequestDTO;
import com.themuffinman.app.business.dto.BusinessGalleryImageResponseDTO;
import com.themuffinman.app.business.service.BusinessGalleryService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/business/gallery")
@RequiredArgsConstructor
public class BusinessGalleryController {

    private final BusinessGalleryService businessGalleryService;

    @GetMapping("/me")
    public BusinessGalleryImageListResponseDTO getMyGallery(@RequestParam(required = false) Long businessProfileId, @AuthenticationPrincipal AppUser currentUser) {
        return businessGalleryService.getMyGallery(currentUser, businessProfileId);
    }

    @PostMapping("/me")
    public BusinessGalleryImageResponseDTO createMyGalleryImage(
            @Valid @RequestBody BusinessGalleryImageRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam(required = false) Long businessProfileId
    ) {
        return businessGalleryService.createMyGalleryImage(dto, currentUser, businessProfileId);
    }

    @PutMapping("/me/{imageId}")
    public BusinessGalleryImageResponseDTO updateMyGalleryImage(
            @PathVariable Long imageId,
            @Valid @RequestBody BusinessGalleryImageRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessGalleryService.updateMyGalleryImage(imageId, dto, currentUser);
    }

    @DeleteMapping("/me/{imageId}")
    public void deleteMyGalleryImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        businessGalleryService.deleteMyGalleryImage(imageId, currentUser);
    }
}
