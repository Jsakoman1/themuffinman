package com.themuffinman.app.things.repository;

import com.themuffinman.app.things.model.ThingListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ThingListingRepository extends JpaRepository<ThingListing, Long> {
    @Query("select tl from ThingListing tl join fetch tl.owner where tl.available = true and tl.archived = false order by tl.createdAt desc, tl.id desc")
    List<ThingListing> findAvailableForCatalog();

    @Query("select tl from ThingListing tl join fetch tl.owner where tl.owner.id = :ownerId order by tl.createdAt desc, tl.id desc")
    List<ThingListing> findForOwnerDashboard(Long ownerId);

    @Query("select tl from ThingListing tl join fetch tl.owner where tl.id = :id")
    Optional<ThingListing> findForListingDetail(Long id);

    default List<ThingListing> findAvailableListings() {
        return findAvailableForCatalog();
    }

    default List<ThingListing> findByOwnerId(Long ownerId) {
        return findForOwnerDashboard(ownerId);
    }

    default Optional<ThingListing> findByIdDetailed(Long id) {
        return findForListingDetail(id);
    }
}
