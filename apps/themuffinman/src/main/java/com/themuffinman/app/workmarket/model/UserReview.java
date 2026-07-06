package com.themuffinman.app.workmarket.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity(name = "WorkmarketUserReview")
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

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_user_id", nullable = false)
    private AppUser reviewer;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
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
