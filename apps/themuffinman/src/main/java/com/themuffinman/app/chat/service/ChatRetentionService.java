package com.themuffinman.app.chat.service;

import com.themuffinman.app.config.RetentionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatRetentionService {

    private final ChatService chatService;
    private final RetentionProperties retentionProperties;

    @Scheduled(cron = "${app.retention.chat.cleanup-cron:0 45 3 * * *}")
    public void cleanupExpiredChatData() {
        int redactedImages = chatService.redactExpiredImages();
        int deletedMessages = chatService.deleteExpiredMessages();

        if (redactedImages > 0 || deletedMessages > 0) {
            log.info(
                    "Chat retention cleanup finished: {} images redacted after {} days, {} messages deleted after {} days",
                    redactedImages,
                    retentionProperties.getChat().getImageDays(),
                    deletedMessages,
                    retentionProperties.getChat().getMessageDays()
            );
        }
    }
}
