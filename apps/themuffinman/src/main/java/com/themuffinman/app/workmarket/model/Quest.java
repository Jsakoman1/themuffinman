package com.themuffinman.app.workmarket.model;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.social.model.CircleGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity(name = "WorkmarketQuest")
@Table(name = "quest")
public class Quest {
    private static final int QUEST_IMAGE_MAX_LENGTH = 350000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private AppUser creator;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "quest_image", joinColumns = @JoinColumn(name = "quest_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "image_data_url", nullable = false, length = QUEST_IMAGE_MAX_LENGTH)
    private List<String> images = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal awardAmount;

    @Column(name = "assignee_target")
    private Integer assigneeTarget = 1;

    @Column(name = "show_approved_applicants", nullable = false)
    private boolean showApprovedApplicants = false;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "term_fixed", nullable = false)
    private boolean termFixed = false;

    @Column(name = "pending_scheduled_at")
    private Instant pendingScheduledAt;

    @Column(name = "pending_ends_at")
    private Instant pendingEndsAt;

    @Column(name = "pending_term_fixed")
    private Boolean pendingTermFixed;

    @Column(name = "reopened_at")
    private Instant reopenedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestAudience audience = QuestAudience.CIRCLES;

    @ManyToMany
    @JoinTable(
            name = "quest_circle_group",
            joinColumns = @JoinColumn(name = "quest_id"),
            inverseJoinColumns = @JoinColumn(name = "circle_id")
    )
    private Set<CircleGroup> visibleToCircles = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "term_change_previous_status")
    private QuestStatus termChangePreviousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestStatus status = QuestStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_visibility", nullable = false)
    private QuestLocationVisibility locationVisibility = QuestLocationVisibility.INHERIT;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_source", nullable = false)
    private QuestLocationSource locationSource = QuestLocationSource.PROFILE;

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

    @Column(name = "location_resolved_at")
    private Instant locationResolvedAt;
}
