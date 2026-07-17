package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessFavoriteResponseDTO;
import com.themuffinman.app.business.service.BusinessFavoriteService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/business/favorites")
@RequiredArgsConstructor
public class BusinessFavoriteController {
    private final BusinessFavoriteService service;

    @GetMapping("/me")
    public List<BusinessFavoriteResponseDTO> getMine(@AuthenticationPrincipal AppUser currentUser) {
        return service.getMine(currentUser);
    }

    @PostMapping("/me/{businessProfileId}")
    public BusinessFavoriteResponseDTO add(@AuthenticationPrincipal AppUser currentUser, @PathVariable Long businessProfileId) {
        return service.add(currentUser, businessProfileId);
    }

    @DeleteMapping("/me/{businessProfileId}")
    public void remove(@AuthenticationPrincipal AppUser currentUser, @PathVariable Long businessProfileId) {
        service.remove(currentUser, businessProfileId);
    }
}
