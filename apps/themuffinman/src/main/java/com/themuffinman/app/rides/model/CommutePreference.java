package com.themuffinman.app.rides.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "commute_preference")
public class CommutePreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "consent_granted", nullable = false)
    private boolean consentGranted;

    @Column(name = "home_area", length = 160)
    private String homeArea;

    @Column(name = "work_area", length = 160)
    private String workArea;

    @Column(length = 32)
    private String weekdays;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "return_time")
    private LocalTime returnTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    void prePersist() { createdAt = Instant.now(); updatedAt = createdAt; }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }
}
