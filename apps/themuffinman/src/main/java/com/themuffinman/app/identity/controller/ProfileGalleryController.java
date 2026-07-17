package com.themuffinman.app.identity.controller;

import com.themuffinman.app.identity.dto.ProfileGalleryImageListResponseDTO;
import com.themuffinman.app.identity.dto.ProfileGalleryImageRequestDTO;
import com.themuffinman.app.identity.dto.ProfileGalleryImageResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.ProfileGalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile/gallery")
@RequiredArgsConstructor
public class ProfileGalleryController {
    private final ProfileGalleryService service;

    @GetMapping("/me")
    public ProfileGalleryImageListResponseDTO getMine(@AuthenticationPrincipal AppUser currentUser) {
        return service.getMyGallery(currentUser);
    }

    @PostMapping("/me")
    public ProfileGalleryImageResponseDTO create(@AuthenticationPrincipal AppUser currentUser, @Valid @RequestBody ProfileGalleryImageRequestDTO dto) {
        return service.create(currentUser, dto);
    }

    @PutMapping("/me/{imageId}")
    public ProfileGalleryImageResponseDTO update(@AuthenticationPrincipal AppUser currentUser, @PathVariable Long imageId, @Valid @RequestBody ProfileGalleryImageRequestDTO dto) {
        return service.update(currentUser, imageId, dto);
    }

    @DeleteMapping("/me/{imageId}")
    public void delete(@AuthenticationPrincipal AppUser currentUser, @PathVariable Long imageId) {
        service.delete(currentUser, imageId);
    }
}
