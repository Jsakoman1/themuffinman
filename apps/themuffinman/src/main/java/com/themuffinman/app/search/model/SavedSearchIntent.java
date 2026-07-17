package com.themuffinman.app.search.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "saved_search_intent")
public class SavedSearchIntent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;
    @Column(name = "query_text", nullable = false, length = 240)
    private String queryText;
    @Column(name = "entity_family", length = 40)
    private String entityFamily;
    @Column(nullable = false) private boolean paused;
    @Column(name = "notify_enabled", nullable = false) private boolean notifyEnabled = true;
    @Column(name = "expires_at") private Instant expiresAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
    @PrePersist void prePersist() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }
}
