package com.themuffinman.app.rides.repository;

import com.themuffinman.app.rides.model.RideAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RideAuditEventRepository extends JpaRepository<RideAuditEvent, Long> {
    List<RideAuditEvent> findByRideIdOrderByCreatedAtAsc(Long rideId);
}
