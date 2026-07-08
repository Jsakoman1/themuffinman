package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessBookingAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessBookingAuditEventRepository extends JpaRepository<BusinessBookingAuditEvent, Long> {
}
