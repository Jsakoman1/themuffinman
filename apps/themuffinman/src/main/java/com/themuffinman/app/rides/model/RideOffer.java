package com.themuffinman.app.rides.model;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.model.CircleGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "ride_offer")
public class RideOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private AppUser driver;

    @Column(nullable = false, length = 140)
    private String origin;

    @Column(nullable = false, length = 140)
    private String destination;

    @Column(name = "departure_at", nullable = false)
    private Instant departureAt;

    @Column(nullable = false)
    private Integer seats = 1;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ride_offer_visible_circle",
            joinColumns = @JoinColumn(name = "ride_offer_id"),
            inverseJoinColumns = @JoinColumn(name = "circle_id")
    )
    private Set<CircleGroup> visibleCircles = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
