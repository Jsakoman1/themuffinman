package com.themuffinman.app.business.model;

import com.themuffinman.app.identity.model.AppUser;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "business_booking")
public class BusinessBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_offering_id", nullable = false)
    private BusinessOffering businessOffering;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_user_id", nullable = false)
    private AppUser customerUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private BusinessBookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private BusinessBookingSource source;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "timezone", nullable = false, length = 80)
    private String timezone;

    @Column(name = "customer_note", length = 1000)
    private String customerNote;

    @Column(name = "owner_note", length = 1000)
    private String ownerNote;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "offering_title_snapshot", nullable = false, length = 120)
    private String offeringTitleSnapshot;

    @Column(name = "price_snapshot_amount", precision = 12, scale = 2)
    private BigDecimal priceSnapshotAmount;

    @Column(name = "price_snapshot_currency", length = 3)
    private String priceSnapshotCurrency;

    @Column(name = "duration_snapshot_minutes", nullable = false)
    private Integer durationSnapshotMinutes;

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
