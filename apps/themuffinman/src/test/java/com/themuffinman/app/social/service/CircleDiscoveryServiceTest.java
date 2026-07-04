package com.themuffinman.app.social.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.location.service.LocationGeoService;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CircleDiscoveryServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private CircleRequestRepository circleRequestRepository;

    @Mock
    private LocationGeoService locationGeoService;

    @Spy
    private CircleViewAssembler circleViewAssembler = new CircleViewAssembler(
            new SocialPresentationHelper(),
            new SocialRelationActionHelper()
    );

    @Spy
    private CircleSearchQueryService circleSearchQueryService = new CircleSearchQueryService();

    @InjectMocks
    private CircleDiscoveryService circleDiscoveryService;

    @Test
    void getNearbyUsersReturnsOnlyDiscoverableUsersWithinRadius() {
        AppUser currentUser = createUser(1L, "owner");
        currentUser.setLocationLatitude(BigDecimal.valueOf(47.0));
        currentUser.setLocationLongitude(BigDecimal.valueOf(8.0));

        AppUser nearby = createUser(2L, "nearby");
        nearby.setLocationLatitude(BigDecimal.valueOf(47.001));
        nearby.setLocationLongitude(BigDecimal.valueOf(8.001));

        AppUser farAway = createUser(3L, "faraway");
        farAway.setLocationLatitude(BigDecimal.valueOf(47.2));
        farAway.setLocationLongitude(BigDecimal.valueOf(8.2));

        when(locationGeoService.isUserDiscoverableNearby(currentUser)).thenReturn(true);
        when(locationGeoService.isUserDiscoverableNearby(nearby)).thenReturn(true);
        when(locationGeoService.isUserDiscoverableNearby(farAway)).thenReturn(true);
        when(locationGeoService.normalizeRadius(2)).thenReturn(2);
        when(locationGeoService.distanceKm(currentUser.getLocationLatitude(), currentUser.getLocationLongitude(), nearby.getLocationLatitude(), nearby.getLocationLongitude()))
                .thenReturn(0.6d);
        when(locationGeoService.distanceKm(currentUser.getLocationLatitude(), currentUser.getLocationLongitude(), farAway.getLocationLatitude(), farAway.getLocationLongitude()))
                .thenReturn(5.4d);
        when(locationGeoService.resolveUserApproximateLocationLabel(nearby)).thenReturn("Villigen, Switzerland");
        when(locationGeoService.resolveUserApproximateLocationLabel(farAway)).thenReturn("Zurich, Switzerland");
        when(appUserRepository.findAll()).thenReturn(List.of(currentUser, nearby, farAway));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(circleRequestRepository.findBetweenUsers(1L, 3L)).thenReturn(Optional.empty());

        CircleSearchResultListResponseDTO result = circleDiscoveryService.getNearbyUsers(currentUser, 2, 0, 12);

        assertEquals(1, result.getItems().size());
        assertEquals(2L, result.getItems().getFirst().getId());
        assertEquals("Villigen, Switzerland", result.getItems().getFirst().getLocationLabel());
        assertEquals("600 m away", result.getItems().getFirst().getDistanceLabel());
    }

    private AppUser createUser(Long id, String username) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setEmail(username + "@example.com");
        appUser.setRole(AppUserRole.USER);
        return appUser;
    }
}
