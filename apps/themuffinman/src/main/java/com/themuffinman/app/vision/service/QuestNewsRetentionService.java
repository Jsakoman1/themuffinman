package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.RetentionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestNewsRetentionService {

    private final QuestNewsService questNewsService;
    private final RetentionProperties retentionProperties;

    @Scheduled(cron = "${app.retention.notifications.cleanup-cron:0 30 3 * * *}")
    public void deleteExpiredNotifications() {
        int deletedCount = questNewsService.deleteExpiredNews();
        if (deletedCount > 0) {
            log.info("Deleted {} expired notification items older than {} days", deletedCount, retentionProperties.getNotifications().getDays());
        }
    }
}
