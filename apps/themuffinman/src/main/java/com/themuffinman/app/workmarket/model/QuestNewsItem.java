package com.themuffinman.app.workmarket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity(name = "WorkmarketQuestNewsItem")
@Table(name = "quest_news_item")
public class QuestNewsItem {

    @jakarta.persistence.Transient
    private boolean canonicalQuestTargetAvailable = true;

    @jakarta.persistence.Transient
    private boolean canonicalApplicationTargetAvailable = true;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_user_id", nullable = false)
    private Long recipientUserId;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "actor_username", nullable = false)
    private String actorUsername;

    @Column(name = "quest_id")
    private Long questId;

    @Column(name = "quest_title")
    private String questTitle;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "circle_request_id")
    private Long circleRequestId;

    @Column(name = "delivery_key", unique = true)
    private String deliveryKey;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private QuestNewsType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
