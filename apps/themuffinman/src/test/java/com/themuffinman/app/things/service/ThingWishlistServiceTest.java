package com.themuffinman.app.things.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.things.dto.ThingWishlistRequestDTO;
import com.themuffinman.app.things.model.ThingListing;
import com.themuffinman.app.things.model.ThingWishlistItem;
import com.themuffinman.app.things.repository.ThingListingRepository;
import com.themuffinman.app.things.repository.ThingWishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThingWishlistServiceTest {
    @Mock ThingWishlistRepository wishlistRepository;
    @Mock ThingListingRepository listingRepository;
    @Mock CircleMembershipService circleMembershipService;
    @InjectMocks ThingWishlistService service;

    @Test
    void savePersistsSelectedOwnedCircles() {
        AppUser owner = user(1L, "owner");
        AppUser listingOwner = user(2L, "lender");
        ThingListing listing = new ThingListing();
        listing.setId(10L);
        listing.setOwner(listingOwner);
        listing.setTitle("Studio monitor");
        CircleGroup circle = new CircleGroup();
        circle.setId(7L);
        when(listingRepository.findForListingDetail(10L)).thenReturn(Optional.of(listing));
        when(circleMembershipService.getOwnedCirclesByIds(owner, List.of(7L))).thenReturn(List.of(circle));
        when(wishlistRepository.findMineForListing(1L, 10L)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(ThingWishlistItem.class))).thenAnswer(invocation -> {
            ThingWishlistItem item = invocation.getArgument(0);
            item.setId(20L);
            return item;
        });

        var result = service.save(owner, 10L, new ThingWishlistRequestDTO(List.of(7L)));

        assertEquals(20L, result.id());
        assertEquals(10L, result.listingId());
        assertEquals(List.of(7L), result.sharedCircleIds());
    }

    private static AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
