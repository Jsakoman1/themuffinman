package com.themuffinman.app.vision.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "vision_conversation")
public class VisionConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VisionIntent intent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VisionConversationStatus status = VisionConversationStatus.ACTIVE;

    @Column(name = "requested_slot", length = 64)
    private String requestedSlot;

    @Convert(converter = VisionSlotDataConverter.class)
    @Column(name = "slot_data", nullable = false, columnDefinition = "TEXT")
    private Map<String, String> slotData = new LinkedHashMap<>();

    @Column(name = "session_memory_snapshot", columnDefinition = "TEXT")
    private String sessionMemorySnapshot;

    @Column(name = "last_user_prompt")
    private String lastUserPrompt;

    @Column(name = "last_normalized_prompt")
    private String lastNormalizedPrompt;

    @Column(name = "last_assistant_message")
    private String lastAssistantMessage;

    @Column(name = "last_translation_reliable", nullable = false)
    private boolean lastTranslationReliable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "last_turn_at", nullable = false)
    private Instant lastTurnAt = Instant.now();
}
