package com.themuffinman.app.things.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingBorrowRequestDTO;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.dto.ThingListingListResponseDTO;
import com.themuffinman.app.things.dto.ThingListingRequestDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.mapper.ThingSharingMgr;
import com.themuffinman.app.things.model.ThingBorrowRequest;
import com.themuffinman.app.things.model.ThingBorrowRequestStatus;
import com.themuffinman.app.things.model.ThingListing;
import com.themuffinman.app.things.repository.ThingBorrowRequestRepository;
import com.themuffinman.app.things.repository.ThingListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThingSharingService {

    private final ThingListingRepository thingListingRepository;
    private final ThingBorrowRequestRepository thingBorrowRequestRepository;
    private final ThingSharingMgr thingSharingMgr;

    public ThingListingListResponseDTO getAvailableListings(AppUser currentUser) {
        List<ThingListing> listings = thingListingRepository.findAvailableForCatalog();
        Map<Long, Long> requestIdByListingId = pendingRequestIds(currentUser, listings);
        return ThingListingListResponseDTO.builder()
                .items(listings.stream()
                        .map(listing -> thingSharingMgr.toListingDto(listing, requestIdByListingId.get(listing.getId())))
                        .toList())
                .build();
    }

    public ThingListingListResponseDTO getMyListings(AppUser currentUser) {
        return ThingListingListResponseDTO.builder()
                .items(thingListingRepository.findForOwnerDashboard(currentUser.getId()).stream()
                        .map(listing -> thingSharingMgr.toListingDto(listing, null))
                        .toList())
                .build();
    }

    @Transactional
    public ThingListingResponseDTO saveMyListing(ThingListingRequestDTO dto, AppUser currentUser) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Thing listing request is required");
        }

        ThingListing listing = new ThingListing();
        listing.setOwner(currentUser);
        applyListingInput(listing, dto);
        return thingSharingMgr.toListingDto(thingListingRepository.save(listing), null);
    }

    @Transactional
    public ThingBorrowRequestResponseDTO requestBorrow(Long listingId, ThingBorrowRequestDTO dto, AppUser currentUser) {
        ThingListing listing = thingListingRepository.findForListingDetail(listingId)
                .orElseThrow(() -> ServiceErrors.notFound("Thing listing not found with id " + listingId));
        if (!listing.isAvailable()) {
            throw ServiceErrors.badRequest("This thing is not currently available");
        }
        if (Objects.equals(listing.getOwner().getId(), currentUser.getId())) {
            throw ServiceErrors.badRequest("Owners cannot request to borrow their own thing");
        }
        if (thingBorrowRequestRepository.existsByListingIdAndBorrowerIdAndStatus(listingId, currentUser.getId(), ThingBorrowRequestStatus.PENDING)) {
            throw ServiceErrors.conflict("You already have a pending borrow request for this thing");
        }

        ThingBorrowRequest request = new ThingBorrowRequest();
        request.setListing(listing);
        request.setBorrower(currentUser);
        request.setMessage(RichTextInputValidator.sanitize(dto == null ? null : dto.getMessage()));
        request.setStatus(ThingBorrowRequestStatus.PENDING);
        return thingSharingMgr.toBorrowRequestDto(thingBorrowRequestRepository.save(request));
    }

    private void applyListingInput(ThingListing listing, ThingListingRequestDTO dto) {
        String title = TextValueNormalizer.requireTrimmed(dto.getTitle(), "Thing title is required");
        listing.setTitle(title);
        listing.setDescription(RichTextInputValidator.sanitize(dto.getDescription()));
        listing.setConditionNote(TextValueNormalizer.trimToNull(dto.getConditionNote()));
        listing.setAvailable(dto.getAvailable() == null || dto.getAvailable());
    }

    private Map<Long, Long> pendingRequestIds(AppUser currentUser, List<ThingListing> listings) {
        if (currentUser == null || listings.isEmpty()) {
            return Map.of();
        }
        List<Long> listingIds = listings.stream().map(ThingListing::getId).toList();
        return thingBorrowRequestRepository.findPendingForCatalogViewer(currentUser.getId(), listingIds, ThingBorrowRequestStatus.PENDING).stream()
                .collect(Collectors.toMap(request -> request.getListing().getId(), ThingBorrowRequest::getId));
    }

}
