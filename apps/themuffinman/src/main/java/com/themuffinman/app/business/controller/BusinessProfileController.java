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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/profiles")
@RequiredArgsConstructor
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    @GetMapping
    public BusinessProfileListResponseDTO getDirectory() {
        return businessProfileService.getDirectory();
    }

    @GetMapping("/me")
    public BusinessProfileResponseDTO getMyProfile(@AuthenticationPrincipal AppUser currentUser) {
        return businessProfileService.getMyProfile(currentUser);
    }

    @PutMapping("/me")
    public BusinessProfileResponseDTO saveMyProfile(
            @Valid @RequestBody BusinessProfileRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessProfileService.saveMyProfile(dto, currentUser);
    }

    @GetMapping("/{slug}")
    public BusinessProfileResponseDTO getProfileBySlug(@PathVariable String slug) {
        return businessProfileService.getProfileBySlug(slug);
    }
}
