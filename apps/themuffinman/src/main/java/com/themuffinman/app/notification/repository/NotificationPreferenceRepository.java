package com.themuffinman.app.notification.repository;

import com.themuffinman.app.notification.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    List<NotificationPreference> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
