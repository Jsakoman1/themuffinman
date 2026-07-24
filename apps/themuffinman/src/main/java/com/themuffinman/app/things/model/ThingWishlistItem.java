package com.themuffinman.app.things.model;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "thing_wishlist_item", uniqueConstraints = @UniqueConstraint(
        name = "uq_thing_wishlist_owner_listing", columnNames = {"owner_id", "listing_id"}))
public class ThingWishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private ThingListing listing;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "thing_wishlist_item_circle",
            joinColumns = @JoinColumn(name = "wishlist_item_id"),
            inverseJoinColumns = @JoinColumn(name = "circle_id"))
    private Set<CircleGroup> sharedCircles = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
