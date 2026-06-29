package com.themuffinman.app.rides.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.dto.RideOfferRequestDTO;
import com.themuffinman.app.rides.mapper.RideOfferMgr;
import com.themuffinman.app.rides.model.RideOffer;
import com.themuffinman.app.rides.repository.RideOfferRepository;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideOfferServiceTest {

    @Mock
    private RideOfferRepository rideOfferRepository;

    @Mock
    private CircleGroupRepository circleGroupRepository;

    @Spy
    private RideOfferMgr rideOfferMgr = new RideOfferMgr();

    @InjectMocks
    private RideOfferService rideOfferService;

    @Test
    void createOfferCreatesCircleScopedRide() {
        AppUser driver = user(1L, "driver");
        CircleGroup circle = new CircleGroup();
        circle.setId(5L);
        circle.setName("Neighbors");
        circle.setOwner(driver);

        when(circleGroupRepository.findAllByOwnerIdAndIdIn(1L, List.of(5L))).thenReturn(List.of(circle));
        when(rideOfferRepository.save(any(RideOffer.class))).thenAnswer(invocation -> {
            RideOffer offer = invocation.getArgument(0);
            offer.setId(20L);
            return offer;
        });

        var result = rideOfferService.createOffer(RideOfferRequestDTO.builder()
                .origin("Zug")
                .destination("Zurich")
                .departureAt(Instant.now().plusSeconds(3600))
                .seats(2)
                .visibleCircleIds(List.of(5L))
                .build(), driver);

        assertEquals(20L, result.getId());
        assertEquals(List.of("Neighbors"), result.getVisibleCircleNames());
    }

    @Test
    void createOfferRejectsUnownedVisibilityCircle() {
        AppUser driver = user(1L, "driver");

        when(circleGroupRepository.findAllByOwnerIdAndIdIn(1L, List.of(5L))).thenReturn(List.of());

        assertThrows(ResponseStatusException.class, () -> rideOfferService.createOffer(RideOfferRequestDTO.builder()
                .origin("Zug")
                .destination("Zurich")
                .departureAt(Instant.now().plusSeconds(3600))
                .seats(2)
                .visibleCircleIds(List.of(5L))
                .build(), driver));
    }

    @Test
    void createOfferRejectsPastDeparture() {
        AppUser driver = user(1L, "driver");

        assertThrows(ResponseStatusException.class, () -> rideOfferService.createOffer(RideOfferRequestDTO.builder()
                .origin("Zug")
                .destination("Zurich")
                .departureAt(Instant.now().minusSeconds(60))
                .seats(2)
                .build(), driver));
    }

    @Test
    void getVisibleOffersMapsRepositoryResults() {
        AppUser driver = user(1L, "driver");
        RideOffer offer = new RideOffer();
        offer.setId(20L);
        offer.setDriver(driver);
        offer.setOrigin("Zug");
        offer.setDestination("Zurich");
        offer.setDepartureAt(Instant.now().plusSeconds(3600));
        offer.setSeats(1);
        offer.setActive(true);

        when(rideOfferRepository.findVisibleActiveOffers(driver.getId())).thenReturn(List.of(offer));

        var result = rideOfferService.getVisibleOffers(driver);

        assertEquals(1, result.getItems().size());
        assertEquals("Zurich", result.getItems().getFirst().getDestination());
    }

    private AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded");
        return user;
    }
}
