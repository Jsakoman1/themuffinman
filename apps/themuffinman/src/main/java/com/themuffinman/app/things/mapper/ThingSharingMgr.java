package com.themuffinman.app.things.mapper;

import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.things.dto.ThingBorrowRequestResponseDTO;
import com.themuffinman.app.things.dto.ThingAllowedActionDTO;
import com.themuffinman.app.things.dto.ThingListingResponseDTO;
import com.themuffinman.app.things.model.ThingBorrowRequest;
import com.themuffinman.app.things.model.ThingListing;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Objects;
import com.themuffinman.app.identity.model.AppUser;

@Component
public class ThingSharingMgr {

    public ThingListingResponseDTO toListingDto(ThingListing listing, Long myPendingRequestId) {
        return toListingDto(listing, myPendingRequestId, null);
    }

    public ThingListingResponseDTO toListingDto(ThingListing listing, Long myPendingRequestId, AppUser viewer) {
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
                .availabilityLabel(listing.isArchived() ? "Archived" : listing.isAvailable() ? "Available to borrow" : "Currently unavailable")
                .allowedActions(allowedListingActions(listing, myPendingRequestId, viewer))
                .build();
    }

    private List<ThingAllowedActionDTO> allowedListingActions(ThingListing listing, Long myPendingRequestId, AppUser viewer) {
        if (viewer == null) return List.of();
        if (Objects.equals(listing.getOwner().getId(), viewer.getId())) {
            return List.of(ThingAllowedActionDTO.EDIT, ThingAllowedActionDTO.ARCHIVE);
        }
        if (myPendingRequestId != null) return List.of(ThingAllowedActionDTO.CANCEL_BORROW_REQUEST);
        return listing.isAvailable() && !listing.isArchived() ? List.of(ThingAllowedActionDTO.REQUEST_BORROW) : List.of();
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
                .stateExplanation(switch (request.getStatus()) {
                    case PENDING -> "Waiting for the owner to decide.";
                    case APPROVED -> "Approved and currently borrowed.";
                    case RETURNED -> "Returned; the thing is available again.";
                    case DECLINED -> "The owner declined this request.";
                    case CANCELLED -> "The borrower cancelled this request.";
                })
                .allowedActions(switch (request.getStatus()) {
                    case PENDING -> List.of(ThingAllowedActionDTO.APPROVE_BORROW_REQUEST, ThingAllowedActionDTO.DECLINE_BORROW_REQUEST, ThingAllowedActionDTO.CANCEL_BORROW_REQUEST);
                    case APPROVED -> List.of(ThingAllowedActionDTO.RETURN_BORROWED_THING);
                    default -> List.of();
                })
                .build();
    }
}
