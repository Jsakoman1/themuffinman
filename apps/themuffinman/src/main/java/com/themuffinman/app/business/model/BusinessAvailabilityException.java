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

import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "business_availability_exception")
public class BusinessAvailabilityException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_offering_id")
    private BusinessOffering businessOffering;

    @Enumerated(EnumType.STRING)
    @Column(name = "exception_type", nullable = false, length = 32)
    private BusinessAvailabilityExceptionType exceptionType = BusinessAvailabilityExceptionType.BLOCK;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "replacement_capacity")
    private Integer replacementCapacity;

    @Column(name = "replacement_start_time_local")
    private LocalTime replacementStartTimeLocal;

    @Column(name = "replacement_end_time_local")
    private LocalTime replacementEndTimeLocal;

    @Column(length = 240)
    private String reason;

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
