package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessProfileListResponseDTO;
import com.themuffinman.app.business.dto.BusinessProfileRequestDTO;
import com.themuffinman.app.business.dto.BusinessProfileResponseDTO;
import com.themuffinman.app.business.service.BusinessProfileService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/business/profiles")
@RequiredArgsConstructor
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    @GetMapping
    public BusinessProfileListResponseDTO getDirectory(
            @RequestParam(value = "q", required = false) String query
    ) {
        return businessProfileService.getDirectory(query);
    }

    @GetMapping("/me")
    public BusinessProfileResponseDTO getMyProfile(@AuthenticationPrincipal AppUser currentUser) {
        return businessProfileService.getMyProfile(currentUser);
    }

    @GetMapping("/me/all")
    public List<BusinessProfileResponseDTO> getMyProfiles(@AuthenticationPrincipal AppUser currentUser) {
        return businessProfileService.getMyProfiles(currentUser);
    }

    @GetMapping("/me/{profileId}")
    public BusinessProfileResponseDTO getMyProfile(@PathVariable Long profileId, @AuthenticationPrincipal AppUser currentUser) {
        return businessProfileService.getMyProfile(profileId, currentUser);
    }

    @PostMapping("/me")
    public BusinessProfileResponseDTO createMyProfile(
            @Valid @RequestBody BusinessProfileRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessProfileService.createMyProfile(dto, currentUser);
    }

    @PutMapping("/me")
    public BusinessProfileResponseDTO saveMyProfile(
            @Valid @RequestBody BusinessProfileRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessProfileService.saveMyProfile(dto, currentUser);
    }

    @PutMapping("/me/{profileId}")
    public BusinessProfileResponseDTO saveMyProfile(
            @PathVariable Long profileId,
            @Valid @RequestBody BusinessProfileRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessProfileService.saveMyProfile(profileId, dto, currentUser);
    }

    @PostMapping("/me/{profileId}/archive")
    public BusinessProfileResponseDTO archiveMyProfile(
            @PathVariable Long profileId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessProfileService.archiveMyProfile(profileId, currentUser);
    }

    @GetMapping("/{slug}")
    public BusinessProfileResponseDTO getProfileBySlug(@PathVariable String slug) {
        return businessProfileService.getProfileBySlug(slug);
    }
}
