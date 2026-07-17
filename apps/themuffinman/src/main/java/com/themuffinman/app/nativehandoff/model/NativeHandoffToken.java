package com.themuffinman.app.nativehandoff.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "native_handoff_token")
public class NativeHandoffToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "target_device", nullable = false, length = 32)
    private String targetDevice;

    @Column(name = "intent", nullable = false, length = 120)
    private String intent;

    @Column(name = "resource_reference", length = 160)
    private String resourceReference;

    @Column(name = "redacted_context", columnDefinition = "TEXT")
    private String redactedContext;

    @Column(nullable = false, length = 64)
    private String nonce;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
