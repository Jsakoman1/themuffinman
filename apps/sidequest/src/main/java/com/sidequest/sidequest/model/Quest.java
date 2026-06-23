package com.sidequest.sidequest.model;

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
@Entity
@Table(name = "quest")
public class Quest {

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
    @Column(name = "image_data_url", nullable = false, length = 12000)
    private List<String> images = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal awardAmount;

    @Column(name = "assignee_target")
    private Integer assigneeTarget = 1;

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

}
