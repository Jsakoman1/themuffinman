package com.themuffinman.app.identity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter @Entity @Table(name = "personal_workspace_shortcut", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "target_type", "target_id"}))
public class PersonalWorkspaceShortcut {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false) private AppUser owner;
    @Column(name = "target_type", nullable = false, length = 40) private String targetType;
    @Column(name = "target_id", nullable = false) private Long targetId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
