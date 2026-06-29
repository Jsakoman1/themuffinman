package com.themuffinman.app.things.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingBorrowRequestDTO;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.dto.ThingListingListResponseDTO;
import com.themuffinman.app.things.dto.ThingListingRequestDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/things")
@RequiredArgsConstructor
public class ThingSharingController {

    private final ThingSharingService thingSharingService;

    @GetMapping("/listings")
    public ThingListingListResponseDTO getAvailableListings(@AuthenticationPrincipal AppUser currentUser) {
        return thingSharingService.getAvailableListings(currentUser);
    }

    @GetMapping("/listings/me")
    public ThingListingListResponseDTO getMyListings(@AuthenticationPrincipal AppUser currentUser) {
        return thingSharingService.getMyListings(currentUser);
    }

    @PostMapping("/listings")
    public ThingListingResponseDTO createListing(
            @Valid @RequestBody ThingListingRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.saveMyListing(dto, currentUser);
    }

    @PostMapping("/listings/{listingId}/borrow-requests")
    public ThingBorrowRequestResponseDTO requestBorrow(
            @PathVariable Long listingId,
            @Valid @RequestBody ThingBorrowRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.requestBorrow(listingId, dto, currentUser);
    }
}
