package com.themuffinman.app.things.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingBorrowRequestDTO;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.dto.ThingListingListResponseDTO;
import com.themuffinman.app.things.dto.ThingListingRequestDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.things.service.ThingPreviewReadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/things")
@RequiredArgsConstructor
public class ThingSharingController {

    private final ThingSharingService thingSharingService;
    private final ThingPreviewReadService thingPreviewReadService;

    @GetMapping("/listings")
    public ThingListingListResponseDTO getAvailableListings(@AuthenticationPrincipal AppUser currentUser) {
        return thingSharingService.getAvailableListings(currentUser);
    }

    @GetMapping("/listings/me")
    public ThingListingListResponseDTO getMyListings(@AuthenticationPrincipal AppUser currentUser) {
        return thingSharingService.getMyListings(currentUser);
    }

    @GetMapping("/listings/{listingId}")
    public ThingListingResponseDTO getListingDetail(
            @PathVariable Long listingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.getListingDetail(listingId, currentUser);
    }

    @GetMapping("/listings/{listingId}/preview")
    public com.themuffinman.app.things.dto.ThingPreviewResponseDTO getListingPreview(
            @PathVariable Long listingId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingPreviewReadService.getPreview(listingId, currentUser);
    }

    @PostMapping("/listings")
    public ThingListingResponseDTO createListing(
            @Valid @RequestBody ThingListingRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.saveMyListing(dto, currentUser);
    }

    @PutMapping("/listings/{listingId}")
    public ThingListingResponseDTO updateListing(
            @PathVariable Long listingId,
            @Valid @RequestBody ThingListingRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.updateMyListingForVision(listingId, dto, currentUser);
    }

    @DeleteMapping("/listings/{listingId}")
    public void archiveListing(@PathVariable Long listingId, @AuthenticationPrincipal AppUser currentUser) {
        thingSharingService.archiveMyListingForVision(listingId, currentUser);
    }

    @PostMapping("/listings/{listingId}/borrow-requests")
    public ThingBorrowRequestResponseDTO requestBorrow(
            @PathVariable Long listingId,
            @Valid @RequestBody ThingBorrowRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.requestBorrow(listingId, dto, currentUser);
    }

    @PatchMapping("/borrow-requests/{requestId}/cancel")
    public ThingBorrowRequestResponseDTO cancelBorrowRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.cancelBorrowRequest(requestId, currentUser);
    }

    @GetMapping("/listings/me/borrow-requests")
    public java.util.List<ThingBorrowRequestResponseDTO> getOwnerBorrowRequests(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.getOwnerBorrowRequests(currentUser);
    }

    @GetMapping("/borrow-requests/me")
    public java.util.List<ThingBorrowRequestResponseDTO> getMyBorrowRequests(
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.getBorrowerRequests(currentUser);
    }

    @PatchMapping("/borrow-requests/{requestId}/decision")
    public ThingBorrowRequestResponseDTO decideBorrowRequest(
            @PathVariable Long requestId,
            @RequestParam boolean approve,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.decideBorrowRequest(requestId, approve, currentUser);
    }

    @PatchMapping("/borrow-requests/{requestId}/return")
    public ThingBorrowRequestResponseDTO returnBorrowedThing(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return thingSharingService.returnBorrowedThing(requestId, currentUser);
    }
}
