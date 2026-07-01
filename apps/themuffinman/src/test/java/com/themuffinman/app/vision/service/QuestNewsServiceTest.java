package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.RetentionProperties;
import com.themuffinman.app.chat.service.ChatRealtimeService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.repository.QuestNewsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestNewsServiceTest {

    @Mock
    private QuestNewsRepository questNewsRepository;

    @Mock
    private RetentionProperties retentionProperties;

    @Mock
    private ChatRealtimeService chatRealtimeService;

    @InjectMocks
    private QuestNewsService questNewsService;

    @Test
    void deleteExpiredNewsUsesConfiguredRetentionWindow() {
        RetentionProperties.Notifications notifications = new RetentionProperties.Notifications();
        notifications.setDays(7);
        when(retentionProperties.getNotifications()).thenReturn(notifications);
        when(questNewsRepository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(5);

        int deletedCount = questNewsService.deleteExpiredNews();

        assertEquals(5, deletedCount);
        verify(questNewsRepository).deleteByCreatedAtBefore(any(Instant.class));
    }

    @Test
    void markMyNewsAsReadPublishesUnreadCountUpdate() {
        AppUser currentUser = new AppUser();
        currentUser.setId(12L);

        when(questNewsRepository.countByRecipientUserIdAndReadAtIsNull(12L)).thenReturn(0L);

        questNewsService.markMyNewsAsRead(currentUser);

        verify(chatRealtimeService).notifyNewsUpdated(12L, 12L, 0L, "news_marked_read");
    }
}
