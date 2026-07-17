package com.themuffinman.app.activity.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @Entity @Table(name = "activity_resume_dismissal", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "resume_key"}))
public class ActivityResumeDismissal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private AppUser user;
    @Column(name = "resume_key", nullable = false, length = 160) private String resumeKey;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
}
