package com.themuffinman.app.vision.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "vision_user_preference", uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(name = "uk_vision_user_preference_user_key", columnNames = {"user_id", "preference_key"})
})
public class VisionUserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "preference_key", nullable = false, length = 80)
    private String preferenceKey;

    @Column(name = "preference_value", nullable = false, length = 255)
    private String preferenceValue;

    @Column(name = "observation_count", nullable = false)
    private int observationCount = 1;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType = "turn";

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore = 0.60d;

    @Column(name = "confidence_updated_at", nullable = false)
    private Instant confidenceUpdatedAt = Instant.now();

    @Column(name = "last_observed_at", nullable = false)
    private Instant lastObservedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
