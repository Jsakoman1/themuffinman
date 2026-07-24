package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessBookingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessBookingSnapshotRepository extends JpaRepository<BusinessBookingSnapshot, Long> {
    Optional<BusinessBookingSnapshot> findByBusinessBookingId(Long businessBookingId);
}
