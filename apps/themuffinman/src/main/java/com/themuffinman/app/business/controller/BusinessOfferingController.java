package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessOfferingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessOfferingRequestDTO;
import com.themuffinman.app.business.dto.BusinessOfferingResponseDTO;
import com.themuffinman.app.business.service.BusinessOfferingService;
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

@RestController
@RequestMapping("/business/offerings")
@RequiredArgsConstructor
public class BusinessOfferingController {

    private final BusinessOfferingService businessOfferingService;

    @GetMapping("/me")
    public BusinessOfferingListResponseDTO getMyOfferings(@AuthenticationPrincipal AppUser currentUser) {
        return businessOfferingService.getMyOfferings(currentUser);
    }

    @PostMapping("/me")
    public BusinessOfferingResponseDTO createMyOffering(
            @Valid @RequestBody BusinessOfferingRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessOfferingService.createMyOffering(dto, currentUser);
    }

    @PutMapping("/{offeringId}/me")
    public BusinessOfferingResponseDTO updateMyOffering(
            @PathVariable Long offeringId,
            @Valid @RequestBody BusinessOfferingRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessOfferingService.updateMyOffering(offeringId, dto, currentUser);
    }

    @DeleteMapping("/{offeringId}/me")
    public void deleteMyOffering(@PathVariable Long offeringId, @AuthenticationPrincipal AppUser currentUser) {
        businessOfferingService.deleteMyOffering(offeringId, currentUser);
    }
}
