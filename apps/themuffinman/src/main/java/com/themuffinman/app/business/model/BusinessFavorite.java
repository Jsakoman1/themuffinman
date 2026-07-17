package com.themuffinman.app.business.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "business_favorite", uniqueConstraints = @UniqueConstraint(name = "uq_business_favorite_owner_business", columnNames = {"owner_id", "business_profile_id"}))
public class BusinessFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
