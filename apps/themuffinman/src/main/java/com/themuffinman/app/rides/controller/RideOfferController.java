package com.themuffinman.app.rides.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.dto.RideOfferListResponseDTO;
import com.themuffinman.app.rides.dto.RideOfferRequestDTO;
import com.themuffinman.app.rides.dto.RideOfferResponseDTO;
import com.themuffinman.app.rides.service.RideOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rides/offers")
@RequiredArgsConstructor
public class RideOfferController {

    private final RideOfferService rideOfferService;

    @GetMapping
    public RideOfferListResponseDTO getVisibleOffers(@AuthenticationPrincipal AppUser currentUser) {
        return rideOfferService.getVisibleOffers(currentUser);
    }

    @GetMapping("/me")
    public RideOfferListResponseDTO getMyOffers(@AuthenticationPrincipal AppUser currentUser) {
        return rideOfferService.getMyOffers(currentUser);
    }

    @PostMapping
    public RideOfferResponseDTO createOffer(
            @Valid @RequestBody RideOfferRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return rideOfferService.createOffer(dto, currentUser);
    }
}
