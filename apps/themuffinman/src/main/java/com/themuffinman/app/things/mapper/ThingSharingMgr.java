package com.themuffinman.app.things.mapper;

import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.model.ThingBorrowRequest;
import com.themuffinman.app.things.model.ThingListing;
import org.springframework.stereotype.Component;

@Component
public class ThingSharingMgr {

    public ThingListingResponseDTO toListingDto(ThingListing listing, Long myPendingRequestId) {
        if (listing == null) {
            return null;
        }

        return ThingListingResponseDTO.builder()
                .id(listing.getId())
                .ownerId(listing.getOwner().getId())
                .ownerUsername(listing.getOwner().getUsername())
                .title(listing.getTitle())
                .description(RichTextInputValidator.sanitize(listing.getDescription()))
                .conditionNote(listing.getConditionNote())
                .available(listing.isAvailable())
                .archived(listing.isArchived())
                .myPendingRequestId(myPendingRequestId)
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }

    public ThingBorrowRequestResponseDTO toBorrowRequestDto(ThingBorrowRequest request) {
        if (request == null) {
            return null;
        }

        return ThingBorrowRequestResponseDTO.builder()
                .id(request.getId())
                .listingId(request.getListing().getId())
                .borrowerId(request.getBorrower().getId())
                .borrowerUsername(request.getBorrower().getUsername())
                .message(RichTextInputValidator.sanitize(request.getMessage()))
                .status(request.getStatus())
                .approvedAt(request.getApprovedAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
