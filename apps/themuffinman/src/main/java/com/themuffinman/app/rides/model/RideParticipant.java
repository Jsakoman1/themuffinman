package com.themuffinman.app.rides.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @Entity @Table(name = "ride_participant", uniqueConstraints = @UniqueConstraint(columnNames = {"ride_offer_id", "passenger_id"}))
public class RideParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ride_offer_id", nullable = false)
    private RideOffer ride;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "passenger_id", nullable = false)
    private AppUser passenger;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private RideParticipantStatus status = RideParticipantStatus.LEFT;
    @Column(name = "joined_at", nullable = false) private Instant joinedAt = Instant.now();
    @Column(name = "left_at") private Instant leftAt;
}
