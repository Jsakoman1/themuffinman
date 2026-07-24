package com.themuffinman.app.things.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.things.dto.ThingWishlistItemResponseDTO;
import com.themuffinman.app.things.dto.ThingWishlistRequestDTO;
import com.themuffinman.app.things.model.ThingListing;
import com.themuffinman.app.things.model.ThingWishlistItem;
import com.themuffinman.app.things.repository.ThingListingRepository;
import com.themuffinman.app.things.repository.ThingWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThingWishlistService {
    private final ThingWishlistRepository wishlistRepository;
    private final ThingListingRepository listingRepository;
    private final CircleMembershipService circleMembershipService;

    public List<ThingWishlistItemResponseDTO> getMine(AppUser currentUser) {
        return wishlistRepository.findMine(currentUser.getId()).stream().map(this::toDto).toList();
    }

    public List<ThingWishlistItemResponseDTO> getSharedWithMe(AppUser currentUser) {
        return wishlistRepository.findSharedWith(currentUser.getId()).stream().map(this::toDto).toList();
    }

    @Transactional
    public ThingWishlistItemResponseDTO save(AppUser currentUser, Long listingId, ThingWishlistRequestDTO request) {
        ThingListing listing = listingRepository.findForListingDetail(listingId)
                .orElseThrow(() -> ServiceErrors.notFound("Thing listing not found"));
        List<Long> circleIds = request == null || request.sharedCircleIds() == null ? List.of() : request.sharedCircleIds();
        List<CircleGroup> circles = circleMembershipService.getOwnedCirclesByIds(currentUser, circleIds);
        ThingWishlistItem item = wishlistRepository.findMineForListing(currentUser.getId(), listingId).orElseGet(() -> {
            ThingWishlistItem created = new ThingWishlistItem();
            created.setOwner(currentUser);
            created.setListing(listing);
            return created;
        });
        item.setSharedCircles(new LinkedHashSet<>(circles));
        return toDto(wishlistRepository.save(item));
    }

    @Transactional
    public void remove(AppUser currentUser, Long listingId) {
        wishlistRepository.findMineForListing(currentUser.getId(), listingId).ifPresent(wishlistRepository::delete);
    }

    private ThingWishlistItemResponseDTO toDto(ThingWishlistItem item) {
        return ThingWishlistItemResponseDTO.builder()
                .id(item.getId())
                .listingId(item.getListing().getId())
                .title(item.getListing().getTitle())
                .ownerUsername(item.getListing().getOwner().getUsername())
                .sharedCircleIds(item.getSharedCircles().stream().map(CircleGroup::getId).toList())
                .savedAt(item.getCreatedAt())
                .build();
    }
}
