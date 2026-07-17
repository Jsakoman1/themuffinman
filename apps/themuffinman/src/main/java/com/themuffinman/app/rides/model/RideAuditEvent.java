package com.themuffinman.app.rides.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter @Entity @Table(name = "ride_audit_event")
public class RideAuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ride_offer_id", nullable = false) private RideOffer ride;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actor_id", nullable = false) private AppUser actor;
    @Column(name = "event_type", nullable = false, length = 40) private String eventType;
    @Enumerated(EnumType.STRING) @Column(name = "from_status", length = 24) private RideStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(name = "to_status", length = 24) private RideStatus toStatus;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "participant_id") private AppUser participant;
    @Column(length = 1000) private String details;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
}
