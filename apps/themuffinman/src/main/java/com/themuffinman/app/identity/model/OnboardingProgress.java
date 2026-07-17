package com.themuffinman.app.identity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @Entity @Table(name = "onboarding_progress")
public class OnboardingProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false, unique = true) private AppUser user;
    @Column(name = "current_step", nullable = false, length = 48) private String currentStep = "WELCOME";
    @Column(nullable = false) private boolean skipped;
    @Column(nullable = false) private boolean completed;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
    @PrePersist @PreUpdate void touch() { updatedAt = Instant.now(); }
}
