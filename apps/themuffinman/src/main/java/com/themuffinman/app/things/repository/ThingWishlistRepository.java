package com.themuffinman.app.things.repository;

import com.themuffinman.app.things.model.ThingWishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ThingWishlistRepository extends JpaRepository<ThingWishlistItem, Long> {
    @Query("select distinct w from ThingWishlistItem w join fetch w.owner join fetch w.listing l left join fetch w.sharedCircles where w.owner.id = :ownerId order by w.createdAt desc, w.id desc")
    List<ThingWishlistItem> findMine(Long ownerId);

    @Query("select distinct w from ThingWishlistItem w join fetch w.owner join fetch w.listing l join w.sharedCircles c join c.memberships membership where membership.member.id = :viewerId order by w.createdAt desc, w.id desc")
    List<ThingWishlistItem> findSharedWith(Long viewerId);

    @Query("select distinct w from ThingWishlistItem w join fetch w.owner join fetch w.listing l left join fetch w.sharedCircles where w.owner.id = :ownerId and w.listing.id = :listingId")
    Optional<ThingWishlistItem> findMineForListing(Long ownerId, Long listingId);
}
