package com.themuffinman.app.vision.model;

import com.themuffinman.app.identity.model.AppUser;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "vision_memory_summary")
public class VisionMemorySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_kind", nullable = false, length = 32)
    private VisionMemorySummaryKind summaryKind;

    @Column(name = "summary_text", nullable = false)
    private String summaryText;

    @Column(name = "source_count", nullable = false)
    private int sourceCount;

    @Column(name = "source_window_started_at")
    private Instant sourceWindowStartedAt;

    @Column(name = "source_window_ended_at")
    private Instant sourceWindowEndedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
