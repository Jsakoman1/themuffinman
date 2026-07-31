package com.themuffinman.app.rides.mapper;

import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.common.dto.ClientActionDTO;
import com.themuffinman.app.common.dto.ClientActionToneDTO;
import com.themuffinman.app.rides.dto.RideOfferResponseDTO;
import com.themuffinman.app.rides.dto.RideAllowedActionDTO;
import com.themuffinman.app.rides.model.RideOffer;
import com.themuffinman.app.rides.model.RideParticipantStatus;
import com.themuffinman.app.rides.repository.RideParticipantRepository;
import com.themuffinman.app.identity.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import com.themuffinman.app.social.model.CircleGroup;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

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
        boolean canJoin = !driver && !joined && offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.OPEN;
        boolean canLeave = joined && (offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.OPEN || offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.FULL);
        boolean canManage = driver && (offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.OPEN || offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.FULL || offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.IN_PROGRESS);
        List<RideAllowedActionDTO> allowedActions = new ArrayList<>();
        if (driver && (offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.OPEN || offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.FULL)) {
            allowedActions.add(RideAllowedActionDTO.EDIT);
            allowedActions.add(RideAllowedActionDTO.CANCEL);
        }
        if (canJoin) allowedActions.add(RideAllowedActionDTO.JOIN);
        if (canLeave) allowedActions.add(RideAllowedActionDTO.LEAVE);
        if (driver && offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.FULL) allowedActions.add(RideAllowedActionDTO.START);
        if (driver && offer.getStatus() == com.themuffinman.app.rides.model.RideStatus.IN_PROGRESS) allowedActions.add(RideAllowedActionDTO.COMPLETE);
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
                .canJoin(canJoin)
                .canLeave(canLeave)
                .canManage(canManage)
                .allowedActions(allowedActions)
                .actions(toClientActions(allowedActions))
                .updatedAt(offer.getUpdatedAt())
                .startedAt(offer.getStartedAt())
                .completedAt(offer.getCompletedAt())
                .cancelledAt(offer.getCancelledAt())
                .visibleCircleNames(offer.getVisibleCircles().stream().map(CircleGroup::getName).sorted().toList())
                .visibleCircleIds(offer.getVisibleCircles().stream().map(CircleGroup::getId).sorted().toList())
                .createdAt(offer.getCreatedAt())
                .build();
    }

    private List<ClientActionDTO> toClientActions(List<RideAllowedActionDTO> allowedActions) {
        return allowedActions.stream().map(action -> {
            boolean destructive = action == RideAllowedActionDTO.CANCEL;
            String label = switch (action) {
                case EDIT -> "Edit ride";
                case JOIN -> "Join ride";
                case LEAVE -> "Leave ride";
                case START -> "Start ride";
                case COMPLETE -> "Complete ride";
                case CANCEL -> "Cancel ride";
            };
            return ClientActionDTO.builder()
                    .id(action.name())
                    .label(label)
                    .tone(destructive ? ClientActionToneDTO.DANGER : action == RideAllowedActionDTO.EDIT || action == RideAllowedActionDTO.LEAVE ? ClientActionToneDTO.SECONDARY : ClientActionToneDTO.PRIMARY)
                    .enabled(true)
                    .requiresConfirmation(action == RideAllowedActionDTO.START || action == RideAllowedActionDTO.COMPLETE || destructive)
                    .confirmationTitle(label)
                    .confirmationMessage(label + "?")
                    .outcome(actionOutcome(action))
                    .build();
        }).toList();
    }

    private String actionOutcome(RideAllowedActionDTO action) {
        return switch (action) {
            case EDIT -> "Your changes are shared with everyone who can see this ride.";
            case JOIN -> "Your seat is reserved and the driver can see you are joining.";
            case LEAVE -> "Your seat becomes available for someone else.";
            case START -> "Participants can see that the journey is in progress.";
            case COMPLETE -> "The ride is recorded as completed.";
            case CANCEL -> "The ride is cancelled and participants are notified.";
        };
    }
}
