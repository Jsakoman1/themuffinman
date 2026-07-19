package com.themuffinman.app.things.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.dto.ThingBorrowRequestDTO;
import com.themuffinman.app.things.dto.ThingListingRequestDTO;
import com.themuffinman.app.things.dto.ThingAllowedActionDTO;
import com.themuffinman.app.things.mapper.ThingSharingMgr;
import com.themuffinman.app.things.model.ThingBorrowRequest;
import com.themuffinman.app.things.model.ThingBorrowRequestStatus;
import com.themuffinman.app.things.model.ThingListing;
import com.themuffinman.app.things.repository.ThingBorrowRequestRepository;
import com.themuffinman.app.things.repository.ThingListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThingSharingServiceTest {

    @Mock
    private ThingListingRepository thingListingRepository;

    @Mock
    private ThingBorrowRequestRepository thingBorrowRequestRepository;

    @Spy
    private ThingSharingMgr thingSharingMgr = new ThingSharingMgr();

    @InjectMocks
    private ThingSharingService thingSharingService;

    @Test
    void saveMyListingCreatesAvailableListingForOwner() {
        AppUser owner = user(1L, "owner");

        when(thingListingRepository.save(any(ThingListing.class))).thenAnswer(invocation -> {
            ThingListing listing = invocation.getArgument(0);
            listing.setId(10L);
            return listing;
        });

        var result = thingSharingService.saveMyListing(ThingListingRequestDTO.builder()
                .title("Ladder")
                .description("<p>Two meters</p>")
                .available(true)
                .build(), owner);

        assertEquals(10L, result.getId());
        assertEquals("Ladder", result.getTitle());
        assertEquals(owner.getId(), result.getOwnerId());
    }

    @Test
    void ownerCanUpdateThingListing() {
        AppUser owner = user(1L, "owner");
        ThingListing listing = listing(10L, owner, true);
        when(thingListingRepository.findForListingDetail(10L)).thenReturn(Optional.of(listing));
        when(thingListingRepository.save(listing)).thenReturn(listing);
        var result = thingSharingService.updateMyListingForVision(10L,
                ThingListingRequestDTO.builder().title("Updated ladder").available(true).build(), owner);
        assertEquals("Updated ladder", result.getTitle());
    }

    @Test
    void ownerCanArchiveThingListingAndItBecomesUnavailable() {
        AppUser owner = user(1L, "owner");
        ThingListing listing = listing(10L, owner, true);
        when(thingListingRepository.findForListingDetail(10L)).thenReturn(Optional.of(listing));
        thingSharingService.archiveMyListingForVision(10L, owner);
        org.junit.jupiter.api.Assertions.assertTrue(listing.isArchived());
        org.junit.jupiter.api.Assertions.assertFalse(listing.isAvailable());
    }

    @Test
    void requestBorrowCreatesPendingRequest() {
        AppUser owner = user(1L, "owner");
        AppUser borrower = user(2L, "borrower");
        ThingListing listing = listing(10L, owner, true);

        when(thingListingRepository.findForListingDetail(10L)).thenReturn(Optional.of(listing));
        when(thingBorrowRequestRepository.existsByListingIdAndBorrowerIdAndStatus(10L, 2L, ThingBorrowRequestStatus.PENDING)).thenReturn(false);
        when(thingBorrowRequestRepository.save(any(ThingBorrowRequest.class))).thenAnswer(invocation -> {
            ThingBorrowRequest request = invocation.getArgument(0);
            request.setId(20L);
            return request;
        });

        var result = thingSharingService.requestBorrow(10L, ThingBorrowRequestDTO.builder().message("Can I borrow it?").build(), borrower);

        assertEquals(20L, result.getId());
        assertEquals(ThingBorrowRequestStatus.PENDING, result.getStatus());
        assertEquals(borrower.getId(), result.getBorrowerId());
    }

    @Test
    void requestBorrowRejectsOwnerRequest() {
        AppUser owner = user(1L, "owner");
        ThingListing listing = listing(10L, owner, true);

        when(thingListingRepository.findForListingDetail(10L)).thenReturn(Optional.of(listing));

        assertThrows(ResponseStatusException.class, () -> thingSharingService.requestBorrow(10L, ThingBorrowRequestDTO.builder().build(), owner));
    }

    @Test
    void requestBorrowRejectsDuplicatePendingRequest() {
        AppUser owner = user(1L, "owner");
        AppUser borrower = user(2L, "borrower");
        ThingListing listing = listing(10L, owner, true);

        when(thingListingRepository.findForListingDetail(10L)).thenReturn(Optional.of(listing));
        when(thingBorrowRequestRepository.existsByListingIdAndBorrowerIdAndStatus(10L, 2L, ThingBorrowRequestStatus.PENDING)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> thingSharingService.requestBorrow(10L, ThingBorrowRequestDTO.builder().build(), borrower));
    }

    @Test
    void getAvailableListingsIncludesCurrentUsersPendingRequest() {
        AppUser owner = user(1L, "owner");
        AppUser borrower = user(2L, "borrower");
        ThingListing listing = listing(10L, owner, true);
        ThingBorrowRequest request = new ThingBorrowRequest();
        request.setId(20L);
        request.setListing(listing);
        request.setBorrower(borrower);
        request.setStatus(ThingBorrowRequestStatus.PENDING);

        when(thingListingRepository.findAvailableForCatalog()).thenReturn(List.of(listing));
        when(thingBorrowRequestRepository.findPendingForCatalogViewer(2L, List.of(10L), ThingBorrowRequestStatus.PENDING)).thenReturn(List.of(request));

        var result = thingSharingService.getAvailableListings(borrower);

        assertEquals(20L, result.getItems().getFirst().getMyPendingRequestId());
        assertEquals(List.of(ThingAllowedActionDTO.CANCEL_BORROW_REQUEST), result.getItems().getFirst().getAllowedActions());
    }

    @Test
    void listingDetailIncludesCurrentUsersPendingRequest() {
        AppUser owner = user(1L, "owner");
        AppUser borrower = user(2L, "borrower");
        ThingListing listing = listing(10L, owner, true);
        ThingBorrowRequest request = request(20L, listing, borrower, ThingBorrowRequestStatus.PENDING);
        when(thingListingRepository.findForListingDetail(10L)).thenReturn(Optional.of(listing));
        when(thingBorrowRequestRepository.findPendingForCatalogViewer(2L, List.of(10L), ThingBorrowRequestStatus.PENDING)).thenReturn(List.of(request));

        var result = thingSharingService.getListingDetail(10L, borrower);

        assertEquals(10L, result.getId());
        assertEquals(20L, result.getMyPendingRequestId());
        assertEquals("Available to borrow", result.getAvailabilityLabel());
        assertEquals(List.of(ThingAllowedActionDTO.CANCEL_BORROW_REQUEST), result.getAllowedActions());
    }

    @Test
    void borrowerCanCancelPendingRequest() {
        AppUser owner = user(1L, "owner");
        AppUser borrower = user(2L, "borrower");
        ThingListing listing = listing(10L, owner, true);
        ThingBorrowRequest request = new ThingBorrowRequest();
        request.setId(20L);
        request.setListing(listing);
        request.setBorrower(borrower);
        request.setStatus(ThingBorrowRequestStatus.PENDING);
        when(thingBorrowRequestRepository.findForBorrowRequestDetail(20L)).thenReturn(Optional.of(request));
        when(thingBorrowRequestRepository.save(request)).thenReturn(request);

        var result = thingSharingService.cancelBorrowRequest(20L, borrower);

        assertEquals(ThingBorrowRequestStatus.CANCELLED, result.getStatus());
    }

    @Test
    void anotherUserCannotCancelBorrowRequest() {
        AppUser owner = user(1L, "owner");
        AppUser borrower = user(2L, "borrower");
        AppUser other = user(3L, "other");
        ThingBorrowRequest request = new ThingBorrowRequest();
        request.setId(20L);
        request.setBorrower(borrower);
        request.setStatus(ThingBorrowRequestStatus.PENDING);
        when(thingBorrowRequestRepository.findForBorrowRequestDetail(20L)).thenReturn(Optional.of(request));

        assertThrows(ResponseStatusException.class, () -> thingSharingService.cancelBorrowRequest(20L, other));
    }

    @Test
    void ownerCanApprovePendingRequestAndMakeListingUnavailable() {
        AppUser owner = user(1L, "owner");
        AppUser borrower = user(2L, "borrower");
        ThingListing listing = listing(10L, owner, true);
        ThingBorrowRequest request = request(20L, listing, borrower, ThingBorrowRequestStatus.PENDING);
        when(thingBorrowRequestRepository.findForBorrowRequestDetail(20L)).thenReturn(Optional.of(request));
        when(thingBorrowRequestRepository.existsByListingIdAndStatus(10L, ThingBorrowRequestStatus.APPROVED)).thenReturn(false);
        when(thingBorrowRequestRepository.save(request)).thenReturn(request);

        var result = thingSharingService.decideBorrowRequest(20L, true, owner);

        assertEquals(ThingBorrowRequestStatus.APPROVED, result.getStatus());
        org.junit.jupiter.api.Assertions.assertFalse(listing.isAvailable());
    }

    @Test
    void borrowerCanReturnApprovedThingAndMakeListingAvailable() {
        AppUser owner = user(1L, "owner");
        AppUser borrower = user(2L, "borrower");
        ThingListing listing = listing(10L, owner, false);
        ThingBorrowRequest request = request(20L, listing, borrower, ThingBorrowRequestStatus.APPROVED);
        when(thingBorrowRequestRepository.findForBorrowRequestDetail(20L)).thenReturn(Optional.of(request));
        when(thingBorrowRequestRepository.save(request)).thenReturn(request);

        var result = thingSharingService.returnBorrowedThing(20L, borrower);

        assertEquals(ThingBorrowRequestStatus.RETURNED, result.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(listing.isAvailable());
    }

    private ThingBorrowRequest request(Long id, ThingListing listing, AppUser borrower, ThingBorrowRequestStatus status) {
        ThingBorrowRequest request = new ThingBorrowRequest();
        request.setId(id);
        request.setListing(listing);
        request.setBorrower(borrower);
        request.setStatus(status);
        return request;
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded");
        return user;
    }

    private ThingListing listing(Long id, AppUser owner, boolean available) {
        ThingListing listing = new ThingListing();
        listing.setId(id);
        listing.setOwner(owner);
        listing.setTitle("Ladder");
        listing.setAvailable(available);
        return listing;
    }
}
