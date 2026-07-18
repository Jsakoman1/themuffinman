package com.themuffinman.app.identity.model;

import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.social.model.CircleGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(length = 2000)
    private String profileDescription;

    @Column(columnDefinition = "TEXT")
    private String profileAvatarDataUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_description_visibility", nullable = false)
    private ProfileFieldVisibility profileDescriptionVisibility = ProfileFieldVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_avatar_visibility", nullable = false)
    private ProfileFieldVisibility profileAvatarVisibility = ProfileFieldVisibility.PUBLIC;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppUserRole role = AppUserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_mode", nullable = false)
    private UserLocationMode locationMode = UserLocationMode.OFF;

    @Enumerated(EnumType.STRING)
    @Column(name = "exact_location_visibility_scope", nullable = false)
    private ExactLocationVisibilityScope exactLocationVisibilityScope = ExactLocationVisibilityScope.NOBODY;

    @Column(name = "location_radius_km", nullable = false)
    private Integer locationRadiusKm = 10;

    @Column(name = "location_label")
    private String locationLabel;

    @Column(name = "location_provider")
    private String locationProvider;

    @Column(name = "location_provider_place_id")
    private String locationProviderPlaceId;

    @Column(name = "location_country_code")
    private String locationCountryCode;

    @Column(name = "location_country")
    private String locationCountry;

    @Column(name = "location_locality")
    private String locationLocality;

    @Column(name = "location_postal_code")
    private String locationPostalCode;

    @Column(name = "location_street")
    private String locationStreet;

    @Column(name = "location_house_number")
    private String locationHouseNumber;

    @Column(name = "location_latitude", precision = 9, scale = 6)
    private BigDecimal locationLatitude;

    @Column(name = "location_longitude", precision = 9, scale = 6)
    private BigDecimal locationLongitude;

    @Column(name = "location_updated_at")
    private Instant locationUpdatedAt;

    @Column(name = "location_resolved_at")
    private Instant locationResolvedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_exact_location_circle",
            joinColumns = @JoinColumn(name = "owner_user_id"),
            inverseJoinColumns = @JoinColumn(name = "circle_id")
    )
    private Set<CircleGroup> exactLocationVisibleToCircles = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_exact_location_user",
            joinColumns = @JoinColumn(name = "owner_user_id"),
            inverseJoinColumns = @JoinColumn(name = "viewer_user_id")
    )
    private Set<AppUser> exactLocationVisibleToUsers = new LinkedHashSet<>();
}
