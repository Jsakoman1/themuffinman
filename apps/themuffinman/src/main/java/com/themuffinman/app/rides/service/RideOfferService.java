package com.themuffinman.app.rides.service;

import com.themuffinman.app.common.concepts.CircleVisibilitySelection;
import com.themuffinman.app.common.concepts.SchedulingWindow;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.rides.dto.*;
import com.themuffinman.app.rides.mapper.RideOfferMgr;
import com.themuffinman.app.rides.model.*;
import com.themuffinman.app.rides.repository.*;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class RideOfferService {
    private final RideOfferRepository rideOfferRepository;
    private final CircleGroupRepository circleGroupRepository;
    private final RideParticipantRepository participantRepository;
    private final RideAuditEventRepository auditRepository;
    private final RideOfferMgr rideOfferMgr;
    private final WorkmarketQuestNewsService questNewsService;

    public RideOfferListResponseDTO getVisibleOffers(AppUser user) {
        return RideOfferListResponseDTO.builder().items(rideOfferRepository.findVisibleActiveOffers(user.getId()).stream().map(r -> rideOfferMgr.toDto(r, user)).toList()).build();
    }
    public RideOfferListResponseDTO findMatchingOffers(AppUser user, String origin, String destination, Instant departureFrom, Instant departureTo) {
        String normalizedOrigin = origin == null ? "" : origin.trim().toLowerCase(Locale.ROOT);
        String normalizedDestination = destination == null ? "" : destination.trim().toLowerCase(Locale.ROOT);
        List<RideOfferResponseDTO> matches = rideOfferRepository.findVisibleActiveOffers(user.getId()).stream()
                .filter(ride -> normalizedOrigin.isBlank() || ride.getOrigin().toLowerCase(Locale.ROOT).contains(normalizedOrigin))
                .filter(ride -> normalizedDestination.isBlank() || ride.getDestination().toLowerCase(Locale.ROOT).contains(normalizedDestination))
                .filter(ride -> departureFrom == null || !ride.getDepartureAt().isBefore(departureFrom))
                .filter(ride -> departureTo == null || !ride.getDepartureAt().isAfter(departureTo))
                .map(ride -> rideOfferMgr.toDto(ride, user)).toList();
        return RideOfferListResponseDTO.builder().items(matches).build();
    }
    public RideOfferListResponseDTO getMyOffers(AppUser user) {
        return RideOfferListResponseDTO.builder().items(rideOfferRepository.findByDriverId(user.getId()).stream().map(r -> rideOfferMgr.toDto(r, user)).toList()).build();
    }
    public RideOfferResponseDTO getOffer(Long id, AppUser user) {
        if (id == null || id <= 0) throw ServiceErrors.badRequest("Ride offer id must be positive");
        RideOffer ride = loadDetailed(id);
        requireVisible(ride, user);
        return rideOfferMgr.toDto(ride, user);
    }

    @Transactional
    public RideOfferResponseDTO createOffer(RideOfferRequestDTO dto, AppUser user) {
        validateFuture(dto);
        RideOffer ride = new RideOffer();
        ride.setDriver(user); applyFields(ride, dto, user);
        ride.setStatus(Boolean.FALSE.equals(dto.getActive()) ? RideStatus.CANCELLED : RideStatus.OPEN);
        ride.setActive(ride.getStatus() != RideStatus.CANCELLED);
        ride = rideOfferRepository.save(ride);
        audit(ride, user, "CREATED", null, ride.getStatus(), null, null);
        return rideOfferMgr.toDto(ride, user);
    }

    @Transactional
    public RideOfferResponseDTO updateOffer(Long id, RideOfferRequestDTO dto, AppUser user) {
        RideOffer ride = loadForUpdate(id); requireDriver(ride, user);
        if (ride.getStatus() != RideStatus.OPEN && ride.getStatus() != RideStatus.FULL) throw ServiceErrors.conflict("Only open rides can be updated");
        validateFuture(dto);
        applyFields(ride, dto, user);
        ride.setActive(true);
        audit(ride, user, "UPDATED", ride.getStatus(), ride.getStatus(), null, null);
        notifyPassengers(ride, user, "Ride updated", "Ride #" + ride.getId() + " was updated by the driver.");
        return rideOfferMgr.toDto(ride, user);
    }

    @Transactional
    public RideOfferResponseDTO updateFromVision(Long id, String origin, String destination, String departureAt, String seats, AppUser user) {
        RideOffer ride = loadForUpdate(id); requireDriver(ride, user);
        if (ride.getStatus() != RideStatus.OPEN && ride.getStatus() != RideStatus.FULL) throw ServiceErrors.conflict("Only open rides can be updated");
        if (origin != null && !origin.isBlank()) ride.setOrigin(TextValueNormalizer.requireTrimmed(origin, "Ride origin is required"));
        if (destination != null && !destination.isBlank()) ride.setDestination(TextValueNormalizer.requireTrimmed(destination, "Ride destination is required"));
        if (departureAt != null && !departureAt.isBlank()) {
            try { ride.setDepartureAt(Instant.parse(departureAt)); } catch (RuntimeException ex) { throw ServiceErrors.badRequest("Ride departure time must be a valid ISO instant"); }
            if (!ride.getDepartureAt().isAfter(TimeSupport.now())) throw ServiceErrors.badRequest("Ride departure time must be in the future");
        }
        if (seats != null && !seats.isBlank()) {
            int requested = Integer.parseInt(seats); long joined = participantRepository.countByRideIdAndStatus(id, RideParticipantStatus.JOINED);
            if (requested < joined || requested < 1 || requested > 8) throw ServiceErrors.conflict("Updated seats cannot be below current passengers or outside 1–8");
            ride.setSeats(requested); if (joined < requested && ride.getStatus() == RideStatus.FULL) ride.setStatus(RideStatus.OPEN);
        }
        audit(ride, user, "UPDATED", ride.getStatus(), ride.getStatus(), null, "Vision update");
        return rideOfferMgr.toDto(ride, user);
    }

    @Transactional
    public RideOfferResponseDTO join(Long id, AppUser user) {
        RideOffer ride = loadForUpdate(id); requireVisible(ride, user);
        if (ride.getDriver().getId().equals(user.getId())) throw ServiceErrors.conflict("The driver cannot join their own ride");
        if (ride.getStatus() != RideStatus.OPEN) throw ServiceErrors.conflict("This ride is not accepting passengers");
        if (!ride.getDepartureAt().isAfter(TimeSupport.now())) throw ServiceErrors.conflict("This ride has already departed");
        RideParticipant participant = participantRepository.findByRideIdAndPassengerId(id, user.getId()).orElseGet(RideParticipant::new);
        // Join is intentionally idempotent for retrying clients: a repeated confirmation must not
        // turn a successful reservation into a misleading error state.
        if (participant.getStatus() == RideParticipantStatus.JOINED) return rideOfferMgr.toDto(ride, user);
        long joined = participantRepository.countByRideIdAndStatus(id, RideParticipantStatus.JOINED);
        if (joined >= ride.getSeats()) throw ServiceErrors.conflict("This ride is full");
        participant.setRide(ride); participant.setPassenger(user); participant.setStatus(RideParticipantStatus.JOINED); participant.setJoinedAt(TimeSupport.now()); participant.setLeftAt(null);
        participantRepository.save(participant);
        RideStatus old = ride.getStatus(); if (joined + 1 >= ride.getSeats()) ride.setStatus(RideStatus.FULL);
        audit(ride, user, "JOINED", old, ride.getStatus(), user, null);
        questNewsService.notifyRideEvent(ride.getDriver(), user, "Passenger joined", user.getUsername() + " joined ride #" + ride.getId() + ".");
        return rideOfferMgr.toDto(ride, user);
    }

    @Transactional
    public RideOfferResponseDTO leave(Long id, AppUser user) {
        RideOffer ride = loadForUpdate(id);
        RideParticipant p = participantRepository.findByRideIdAndPassengerId(id, user.getId()).orElseThrow(() -> ServiceErrors.conflict("You are not a passenger on this ride"));
        if (p.getStatus() != RideParticipantStatus.JOINED) throw ServiceErrors.conflict("You already left this ride");
        if (ride.getStatus() != RideStatus.OPEN && ride.getStatus() != RideStatus.FULL) throw ServiceErrors.conflict("Passengers cannot leave after the ride starts");
        p.setStatus(RideParticipantStatus.LEFT); p.setLeftAt(TimeSupport.now());
        RideStatus old = ride.getStatus(); if (old == RideStatus.FULL) ride.setStatus(RideStatus.OPEN);
        audit(ride, user, "LEFT", old, ride.getStatus(), user, null);
        questNewsService.notifyRideEvent(ride.getDriver(), user, "Passenger left", user.getUsername() + " left ride #" + ride.getId() + ".");
        return rideOfferMgr.toDto(ride, user);
    }

    @Transactional public RideOfferResponseDTO cancel(Long id, AppUser user) {
        RideOffer ride = loadForUpdate(id); requireDriver(ride, user);
        if (ride.getStatus() != RideStatus.OPEN && ride.getStatus() != RideStatus.FULL) throw ServiceErrors.conflict("Only open rides can be cancelled");
        RideStatus old = ride.getStatus(); ride.setStatus(RideStatus.CANCELLED); ride.setActive(false); ride.setCancelledAt(TimeSupport.now());
        audit(ride, user, "CANCELLED", old, ride.getStatus(), null, "Driver cancelled the ride");
        notifyPassengers(ride, user, "Ride cancelled", "Ride #" + ride.getId() + " was cancelled by the driver.");
        return rideOfferMgr.toDto(ride, user);
    }
    @Transactional public RideOfferResponseDTO start(Long id, AppUser user) {
        RideOffer ride = loadForUpdate(id); requireDriver(ride, user);
        if (ride.getStatus() != RideStatus.FULL) throw ServiceErrors.conflict("A ride can start only when full");
        RideStatus old = ride.getStatus(); ride.setStatus(RideStatus.IN_PROGRESS); ride.setStartedAt(TimeSupport.now()); audit(ride, user, "STARTED", old, ride.getStatus(), null, null); notifyPassengers(ride, user, "Ride started", "Ride #" + ride.getId() + " has started."); return rideOfferMgr.toDto(ride, user);
    }
    @Transactional public RideOfferResponseDTO complete(Long id, AppUser user) {
        RideOffer ride = loadForUpdate(id); requireDriver(ride, user);
        if (ride.getStatus() != RideStatus.IN_PROGRESS) throw ServiceErrors.conflict("Only an in-progress ride can be completed");
        RideStatus old = ride.getStatus(); ride.setStatus(RideStatus.COMPLETED); ride.setCompletedAt(TimeSupport.now()); ride.setActive(false); audit(ride, user, "COMPLETED", old, ride.getStatus(), null, null); notifyPassengers(ride, user, "Ride completed", "Ride #" + ride.getId() + " was completed."); return rideOfferMgr.toDto(ride, user);
    }

    private RideOffer loadDetailed(Long id) { return rideOfferRepository.findDetailedById(id).orElseThrow(() -> ServiceErrors.notFound("Ride offer not found")); }
    private RideOffer loadForUpdate(Long id) { return rideOfferRepository.findByIdForUpdate(id).orElseThrow(() -> ServiceErrors.notFound("Ride offer not found")); }
    private void requireDriver(RideOffer ride, AppUser user) { if (!ride.getDriver().getId().equals(user.getId())) throw ServiceErrors.forbidden("Only the driver can manage this ride"); }
    private void requireVisible(RideOffer ride, AppUser user) {
        if (ride.getDriver().getId().equals(user.getId()) || ride.getVisibleCircles().isEmpty()) return;
        boolean member = ride.getVisibleCircles().stream().anyMatch(c -> c.getMemberships().stream().anyMatch(m -> m.getMember().getId().equals(user.getId())));
        if (!member) throw ServiceErrors.forbidden("This ride is not visible to you");
    }
    private void validateFuture(RideOfferRequestDTO dto) {
        if (dto == null || dto.getDepartureAt() == null || new SchedulingWindow(dto.getDepartureAt(), null).startsBefore(TimeSupport.now())) throw ServiceErrors.badRequest("Ride departure time must be in the future");
    }
    private void applyFields(RideOffer ride, RideOfferRequestDTO dto, AppUser user) {
        ride.setOrigin(TextValueNormalizer.requireTrimmed(dto.getOrigin(), "Ride origin is required")); ride.setDestination(TextValueNormalizer.requireTrimmed(dto.getDestination(), "Ride destination is required")); ride.setDepartureAt(dto.getDepartureAt()); ride.setSeats(dto.getSeats() == null ? 1 : dto.getSeats()); ride.setNote(RichTextInputValidator.sanitize(dto.getNote())); ride.setVisibleCircles(new LinkedHashSet<>(resolveVisibleCircles(dto.getVisibleCircleIds(), user)));
    }
    private List<CircleGroup> resolveVisibleCircles(List<Long> ids, AppUser user) {
        CircleVisibilitySelection selection = CircleVisibilitySelection.from(ids); if (selection.unrestricted()) return List.of();
        List<CircleGroup> circles = circleGroupRepository.findAllByOwnerIdAndIdIn(user.getId(), List.copyOf(selection.asDistinctSet())); if (circles.size() != selection.distinctCount()) throw ServiceErrors.badRequest("Ride visibility circles must be owned by the driver"); return circles;
    }
    private void audit(RideOffer ride, AppUser actor, String type, RideStatus from, RideStatus to, AppUser participant, String details) { if (auditRepository == null) return; RideAuditEvent e = new RideAuditEvent(); e.setRide(ride); e.setActor(actor); e.setEventType(type); e.setFromStatus(from); e.setToStatus(to); e.setParticipant(participant); e.setDetails(details); auditRepository.save(e); }
    private void notifyPassengers(RideOffer ride, AppUser actor, String title, String message) { participantRepository.findActiveByRideId(ride.getId()).forEach(p -> questNewsService.notifyRideEvent(p.getPassenger(), actor, title, message)); }
}
