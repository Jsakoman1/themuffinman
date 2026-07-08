package com.themuffinman.app.rides.service;

import com.themuffinman.app.common.concepts.CircleVisibilitySelection;
import com.themuffinman.app.common.concepts.SchedulingWindow;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.dto.RideOfferListResponseDTO;
import com.themuffinman.app.rides.dto.RideOfferRequestDTO;
import com.themuffinman.app.rides.dto.RideOfferResponseDTO;
import com.themuffinman.app.rides.mapper.RideOfferMgr;
import com.themuffinman.app.rides.model.RideOffer;
import com.themuffinman.app.rides.repository.RideOfferRepository;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RideOfferService {

    private final RideOfferRepository rideOfferRepository;
    private final CircleGroupRepository circleGroupRepository;
    private final RideOfferMgr rideOfferMgr;

    public RideOfferListResponseDTO getVisibleOffers(AppUser currentUser) {
        return RideOfferListResponseDTO.builder()
                .items(rideOfferRepository.findVisibleActiveOffers(currentUser.getId()).stream()
                        .map(rideOfferMgr::toDto)
                        .toList())
                .build();
    }

    public RideOfferListResponseDTO getMyOffers(AppUser currentUser) {
        return RideOfferListResponseDTO.builder()
                .items(rideOfferRepository.findByDriverId(currentUser.getId()).stream()
                        .map(rideOfferMgr::toDto)
                        .toList())
                .build();
    }

    @Transactional
    public RideOfferResponseDTO createOffer(RideOfferRequestDTO dto, AppUser currentUser) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Ride offer request is required");
        }
        if (new SchedulingWindow(dto.getDepartureAt(), null).startsBefore(TimeSupport.now())) {
            throw ServiceErrors.badRequest("Ride departure time must be in the future");
        }

        RideOffer offer = new RideOffer();
        offer.setDriver(currentUser);
        offer.setOrigin(TextValueNormalizer.requireTrimmed(dto.getOrigin(), "Ride origin is required"));
        offer.setDestination(TextValueNormalizer.requireTrimmed(dto.getDestination(), "Ride destination is required"));
        offer.setDepartureAt(dto.getDepartureAt());
        offer.setSeats(dto.getSeats() == null ? 1 : dto.getSeats());
        offer.setNote(RichTextInputValidator.sanitize(dto.getNote()));
        offer.setActive(dto.getActive() == null || dto.getActive());
        offer.setVisibleCircles(new LinkedHashSet<>(resolveVisibleCircles(dto.getVisibleCircleIds(), currentUser)));

        return rideOfferMgr.toDto(rideOfferRepository.save(offer));
    }

    private List<CircleGroup> resolveVisibleCircles(List<Long> circleIds, AppUser currentUser) {
        CircleVisibilitySelection selection = CircleVisibilitySelection.from(circleIds);
        if (selection.unrestricted()) {
            return List.of();
        }
        List<CircleGroup> circles = circleGroupRepository.findAllByOwnerIdAndIdIn(currentUser.getId(), List.copyOf(selection.asDistinctSet()));
        if (circles.size() != selection.distinctCount()) {
            throw ServiceErrors.badRequest("Ride visibility circles must be owned by the driver");
        }
        return circles;
    }
}
