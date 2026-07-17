package com.themuffinman.app.notification.model;

import com.themuffinman.app.identity.model.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notification_preference", uniqueConstraints = @UniqueConstraint(name = "uq_notification_preference_user_category_level", columnNames = {"user_id", "category", "level"}))
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationPreferenceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationPreferenceLevel level;

    @Column(nullable = false)
    private boolean enabled = true;
}
