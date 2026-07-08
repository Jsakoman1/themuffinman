package com.themuffinman.app.business.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "business_booking_policy")
public class BusinessBookingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id", nullable = false, unique = true)
    private BusinessProfile businessProfile;

    @Column(name = "lead_time_minutes", nullable = false)
    private int leadTimeMinutes;

    @Column(name = "max_advance_days", nullable = false)
    private int maxAdvanceDays;

    @Column(name = "customer_cancellation_window_minutes", nullable = false)
    private int customerCancellationWindowMinutes;

    @Column(name = "owner_reschedule_window_minutes", nullable = false)
    private int ownerRescheduleWindowMinutes;

    @Column(name = "requires_owner_confirmation_default", nullable = false)
    private boolean requiresOwnerConfirmationDefault = true;

    @Column(name = "allow_customer_cancellation", nullable = false)
    private boolean allowCustomerCancellation = true;

    @Column(name = "allow_owner_manual_approval", nullable = false)
    private boolean allowOwnerManualApproval = true;

    @Column(name = "allow_owner_manual_rejection", nullable = false)
    private boolean allowOwnerManualRejection = true;

    @Column(name = "allow_waitlist", nullable = false)
    private boolean allowWaitlist;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
