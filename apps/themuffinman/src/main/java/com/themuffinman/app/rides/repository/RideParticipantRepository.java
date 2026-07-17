package com.themuffinman.app.rides.repository;

import com.themuffinman.app.rides.model.RideParticipant;
import com.themuffinman.app.rides.model.RideParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RideParticipantRepository extends JpaRepository<RideParticipant, Long> {
    Optional<RideParticipant> findByRideIdAndPassengerId(Long rideId, Long passengerId);
    List<RideParticipant> findByRideIdAndStatus(Long rideId, RideParticipantStatus status);
    long countByRideIdAndStatus(Long rideId, RideParticipantStatus status);
    @Query("select rp from RideParticipant rp join fetch rp.passenger where rp.ride.id = :rideId and rp.status = 'JOINED'")
    List<RideParticipant> findActiveByRideId(Long rideId);
}
