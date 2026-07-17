package com.themuffinman.app.trust.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @Entity @Table(name = "safety_report")
public class SafetyReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "reporter_id", nullable = false) private AppUser reporter;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "target_user_id") private AppUser targetUser;
    @Column(name = "target_family", nullable = false, length = 40) private String targetFamily;
    @Column(name = "target_id") private Long targetId;
    @Column(nullable = false, length = 1000) private String reason;
    @Column(nullable = false, length = 24) private String status = "OPEN";
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
    @PrePersist void prePersist() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}
