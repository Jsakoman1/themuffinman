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
import java.time.Instant;

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

    @GetMapping("/matches")
    public RideOfferListResponseDTO findMatches(@RequestParam(required = false) String origin,
                                                @RequestParam(required = false) String destination,
                                                @RequestParam(required = false) Instant departureFrom,
                                                @RequestParam(required = false) Instant departureTo,
                                                @AuthenticationPrincipal AppUser currentUser) {
        return rideOfferService.findMatchingOffers(currentUser, origin, destination, departureFrom, departureTo);
    }

    @GetMapping("/{id}")
    public RideOfferResponseDTO getOffer(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        return rideOfferService.getOffer(id, currentUser);
    }

    @PostMapping
    public RideOfferResponseDTO createOffer(
            @Valid @RequestBody RideOfferRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return rideOfferService.createOffer(dto, currentUser);
    }

    @PutMapping("/{id}")
    public RideOfferResponseDTO updateOffer(@PathVariable Long id, @Valid @RequestBody RideOfferRequestDTO dto, @AuthenticationPrincipal AppUser currentUser) {
        return rideOfferService.updateOffer(id, dto, currentUser);
    }

    @PostMapping("/{id}/join")
    public RideOfferResponseDTO join(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) { return rideOfferService.join(id, currentUser); }

    @PostMapping("/{id}/leave")
    public RideOfferResponseDTO leave(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) { return rideOfferService.leave(id, currentUser); }

    @PostMapping("/{id}/cancel")
    public RideOfferResponseDTO cancel(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) { return rideOfferService.cancel(id, currentUser); }

    @PostMapping("/{id}/start")
    public RideOfferResponseDTO start(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) { return rideOfferService.start(id, currentUser); }

    @PostMapping("/{id}/complete")
    public RideOfferResponseDTO complete(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) { return rideOfferService.complete(id, currentUser); }
}
