package com.themuffinman.app.workmarket.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "user_review",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_review_quest_reviewer_reviewed",
                        columnNames = {"quest_id", "reviewer_user_id", "reviewed_user_id"}
                )
        }
)
public class UserReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_user_id", nullable = false)
    private AppUser reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewed_user_id", nullable = false)
    private AppUser reviewedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewed_role", nullable = false)
    private ReviewRole reviewedRole;

    @Column(nullable = false)
    private Short stars;

    @Column(length = 500)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
