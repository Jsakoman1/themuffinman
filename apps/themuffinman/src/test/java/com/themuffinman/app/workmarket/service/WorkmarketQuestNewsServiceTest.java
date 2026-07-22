package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.chat.service.ChatRealtimeService;
import com.themuffinman.app.config.RetentionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.notification.service.NotificationPreferenceService;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestNewsRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class WorkmarketQuestNewsServiceTest {

    @Test
    void doesNotInsertDuplicateApplicationNewsForTheSameEvent() {
        WorkmarketQuestNewsRepository repository = mock(WorkmarketQuestNewsRepository.class);
        when(repository.existsByDeliveryKey("workmarket-application-news:00000000-0000-0000-0000-000000000001")).thenReturn(true);
        WorkmarketQuestNewsService service = new WorkmarketQuestNewsService(
                repository,
                mock(WorkmarketQuestApplicationRepository.class),
                mock(WorkmarketQuestRepository.class),
                mock(RetentionProperties.class),
                mock(ChatRealtimeService.class),
                mock(NotificationPreferenceService.class)
        );

        AppUser owner = new AppUser();
        owner.setId(1L);
        AppUser applicant = new AppUser();
        applicant.setId(2L);
        applicant.setUsername("applicant");
        Quest quest = new Quest();
        quest.setId(10L);
        quest.setTitle("Move sofa");
        quest.setCreator(owner);
        QuestApplication application = new QuestApplication();
        application.setId(20L);
        application.setQuest(quest);
        application.setApplicant(applicant);

        service.notifyApplicationCreated(quest, application, applicant, UUID.fromString("00000000-0000-0000-0000-000000000001"));

        verify(repository).existsByDeliveryKey("workmarket-application-news:00000000-0000-0000-0000-000000000001");
        verify(repository, never()).save(any());
    }
}
