package com.themuffinman.app.vision.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "quest_news_item")
public class QuestNewsItem {

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

    @Enumerated(EnumType.STRING)
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
