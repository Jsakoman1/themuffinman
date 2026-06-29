package com.themuffinman.app.things.repository;

import com.themuffinman.app.things.model.ThingBorrowRequest;
import com.themuffinman.app.things.model.ThingBorrowRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ThingBorrowRequestRepository extends JpaRepository<ThingBorrowRequest, Long> {
    boolean existsByListingIdAndBorrowerIdAndStatus(Long listingId, Long borrowerId, ThingBorrowRequestStatus status);

    @Query("select tr from ThingBorrowRequest tr join fetch tr.listing l join fetch l.owner join fetch tr.borrower where tr.listing.id in :listingIds and tr.borrower.id = :borrowerId and tr.status = :status")
    List<ThingBorrowRequest> findPendingForCatalogViewer(Long borrowerId, List<Long> listingIds, ThingBorrowRequestStatus status);

    @Query("select tr from ThingBorrowRequest tr join fetch tr.listing l join fetch l.owner join fetch tr.borrower where tr.id = :id")
    Optional<ThingBorrowRequest> findForBorrowRequestDetail(Long id);

    default List<ThingBorrowRequest> findByListingIdsAndBorrower(
            Long borrowerId,
            List<Long> listingIds,
            ThingBorrowRequestStatus status
    ) {
        return findPendingForCatalogViewer(borrowerId, listingIds, status);
    }

    default Optional<ThingBorrowRequest> findByIdDetailed(Long id) {
        return findForBorrowRequestDetail(id);
    }
}
