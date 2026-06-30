package com.themuffinman.app.vision.model;

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
@Table(name = "vision_turn")
public class VisionTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private VisionConversation conversation;

    @Column(name = "turn_index", nullable = false)
    private int turnIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private VisionTurnSource source;

    @Column(nullable = false)
    private String prompt;

    @Column(name = "normalized_prompt", nullable = false)
    private String normalizedPrompt;

    @Enumerated(EnumType.STRING)
    @Column(name = "detected_intent", nullable = false, length = 32)
    private VisionIntent detectedIntent;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_state", nullable = false, length = 32)
    private VisionAgentState agentState;

    @Enumerated(EnumType.STRING)
    @Column(name = "next_action", nullable = false, length = 32)
    private VisionNextAction nextAction;

    @Column(name = "requested_slot", length = 64)
    private String requestedSlot;

    @Column(name = "translation_applied", nullable = false)
    private boolean translationApplied;

    @Column(name = "translation_reliable", nullable = false)
    private boolean translationReliable = true;

    @Column(name = "assistant_message", nullable = false)
    private String assistantMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
