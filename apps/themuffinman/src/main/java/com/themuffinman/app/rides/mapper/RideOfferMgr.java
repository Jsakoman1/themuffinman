package com.themuffinman.app.rides.mapper;

import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.rides.dto.RideOfferResponseDTO;
import com.themuffinman.app.rides.model.RideOffer;
import com.themuffinman.app.rides.model.RideParticipantStatus;
import com.themuffinman.app.rides.repository.RideParticipantRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import com.themuffinman.app.social.model.CircleGroup;
import org.springframework.stereotype.Component;

@Component
public class RideOfferMgr {
    private RideParticipantRepository participantRepository;

    public RideOfferMgr() { }
    @Autowired
    public RideOfferMgr(RideParticipantRepository participantRepository) { this.participantRepository = participantRepository; }

    public RideOfferResponseDTO toDto(RideOffer offer) { return toDto(offer, null); }
    public RideOfferResponseDTO toDto(RideOffer offer, AppUser viewer) {
        int joinedSeats = participantRepository == null || offer.getId() == null ? 0 : (int) participantRepository.countByRideIdAndStatus(offer.getId(), RideParticipantStatus.JOINED);
        boolean driver = viewer != null && offer.getDriver().getId().equals(viewer.getId());
        boolean joined = viewer != null && participantRepository != null && offer.getId() != null && participantRepository.findByRideIdAndPassengerId(offer.getId(), viewer.getId())
                .map(p -> p.getStatus() == RideParticipantStatus.JOINED).orElse(false);
        return RideOfferResponseDTO.builder()
                .id(offer.getId())
                .driverId(offer.getDriver().getId())
                .driverUsername(offer.getDriver().getUsername())
                .origin(offer.getOrigin())
                .destination(offer.getDestination())
                .departureAt(offer.getDepartureAt())
                .seats(offer.getSeats())
                .note(RichTextInputValidator.sanitize(offer.getNote()))
                .active(offer.isActive())
                .status(offer.getStatus())
                .joinedSeats(joinedSeats)
                .viewerJoined(joined)
                .viewerIsDriver(driver)
                .canJoin(!driver && !joined && (offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.OPEN))
                .canLeave(joined && (offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.OPEN || offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.FULL))
                .canManage(driver && (offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.OPEN || offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.FULL || offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.IN_PROGRESS))
                .updatedAt(offer.getUpdatedAt())
                .startedAt(offer.getStartedAt())
                .completedAt(offer.getCompletedAt())
                .cancelledAt(offer.getCancelledAt())
                .visibleCircleNames(offer.getVisibleCircles().stream().map(CircleGroup::getName).sorted().toList())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}
