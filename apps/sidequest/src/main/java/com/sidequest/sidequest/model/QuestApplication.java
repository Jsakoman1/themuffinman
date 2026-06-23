package com.sidequest.sidequest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "quest_application", uniqueConstraints = {@UniqueConstraint(name = "uk_quest_application_quest_applicant", columnNames = {"quest_id", "applicant_id"})})
public class QuestApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private AppUser applicant;

    @Column(length = 2000)
    private String message;

    @Column(precision = 10, scale = 2)
    private BigDecimal proposedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestApplicationStatus status = QuestApplicationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}