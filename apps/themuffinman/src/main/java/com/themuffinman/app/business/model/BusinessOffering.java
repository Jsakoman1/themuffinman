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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "business_offering")
public class BusinessOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 140)
    private String slug;

    @Column(length = 240)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false, length = 32)
    private BusinessOfferingPricingType pricingType = BusinessOfferingPricingType.FIXED;

    @Column(name = "base_price_amount", precision = 12, scale = 2)
    private BigDecimal basePriceAmount;

    @Column(name = "base_price_currency", length = 3)
    private String basePriceCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_mode", nullable = false, length = 32)
    private BusinessOfferingDurationMode durationMode = BusinessOfferingDurationMode.FIXED;

    @Column(name = "default_duration_minutes")
    private Integer defaultDurationMinutes;

    @Column(name = "min_duration_minutes")
    private Integer minDurationMinutes;

    @Column(name = "max_duration_minutes")
    private Integer maxDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "capacity_mode", nullable = false, length = 32)
    private BusinessOfferingCapacityMode capacityMode = BusinessOfferingCapacityMode.SINGLE;

    @Column(name = "slot_capacity", nullable = false)
    private Integer slotCapacity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_mode", nullable = false, length = 32)
    private BusinessOfferingBookingMode bookingMode = BusinessOfferingBookingMode.REQUEST;

    @Column(name = "requires_owner_confirmation", nullable = false)
    private boolean requiresOwnerConfirmation = true;

    @Column(name = "buffer_before_minutes", nullable = false)
    private int bufferBeforeMinutes;

    @Column(name = "buffer_after_minutes", nullable = false)
    private int bufferAfterMinutes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

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
