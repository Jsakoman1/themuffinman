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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;

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

    @PostMapping(value = "/me/upload", consumes = "multipart/form-data")
    public BusinessGalleryImageResponseDTO uploadMyGalleryImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "altText", required = false) String altText,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) Long businessProfileId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessGalleryService.uploadMyGalleryImage(file, altText, sortOrder, currentUser, businessProfileId);
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

    @GetMapping("/object")
    public ResponseEntity<ByteArrayResource> getPublicImage(@RequestParam("key") String storageKey) {
        var image = businessGalleryService.getPublicImage(storageKey);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType())).contentLength(image.content().length).body(new ByteArrayResource(image.content()));
    }
}
