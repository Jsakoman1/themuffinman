package com.themuffinman.app.workmarket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "WorkmarketApplicationNewsOutbox")
@Table(name = "workmarket_application_news_outbox")
public class WorkmarketApplicationNewsOutbox {

    public enum Status { PENDING, PROCESSING, FAILED, DELIVERED }

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private WorkmarketQuestApplicationNewsEventType eventType;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "lease_owner", length = 100)
    private String leaseOwner;

    @Column(name = "lease_until")
    private Instant leaseUntil;

    @Column(name = "replay_reference", length = 160)
    private String replayReference;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
