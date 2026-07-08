package com.themuffinman.app.business.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "business_profile")
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(name = "business_name", nullable = false, length = 120)
    private String businessName;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(length = 160)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_email", length = 160)
    private String contactEmail;

    @Column(name = "contact_phone", length = 80)
    private String contactPhone;

    @Column(name = "website_url", length = 300)
    private String websiteUrl;

    @Column(length = 80)
    private String timezone;

    @Column(name = "booking_enabled", nullable = false)
    private boolean bookingEnabled;

    @Column(name = "public_address_label", length = 240)
    private String publicAddressLabel;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "contact_whatsapp", length = 80)
    private String contactWhatsapp;

    @Column(name = "hero_image_url", length = 500)
    private String heroImageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
