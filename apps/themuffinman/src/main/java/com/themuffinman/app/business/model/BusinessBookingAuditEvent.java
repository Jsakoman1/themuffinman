package com.themuffinman.app.business.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "business_booking_audit_event")
public class BusinessBookingAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_booking_id", nullable = false)
    private BusinessBooking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private BusinessBookingAuditEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 40)
    private BusinessBookingStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 40)
    private BusinessBookingStatus newStatus;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_username", length = 120)
    private String actorUsername;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
