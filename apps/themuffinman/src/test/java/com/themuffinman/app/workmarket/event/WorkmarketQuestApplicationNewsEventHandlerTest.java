package com.themuffinman.app.workmarket.event;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkmarketQuestApplicationNewsEventHandlerTest {

    @Test
    void reloadsApplicationAndActorBeforeDeliveringAfterCommitNews() {
        WorkmarketQuestNewsService newsService = mock(WorkmarketQuestNewsService.class);
        WorkmarketQuestApplicationRepository applicationRepository = mock(WorkmarketQuestApplicationRepository.class);
        AppUserRepository userRepository = mock(AppUserRepository.class);
        WorkmarketQuestApplicationNewsEventHandler handler = new WorkmarketQuestApplicationNewsEventHandler(
                newsService,
                applicationRepository,
                userRepository
        );

        QuestApplication detachedApplication = new QuestApplication();
        detachedApplication.setId(42L);
        AppUser detachedActor = new AppUser();
        detachedActor.setId(7L);
        WorkmarketQuestApplicationNewsEvent event = new WorkmarketQuestApplicationNewsEvent(
                WorkmarketQuestApplicationNewsEvent.Type.CREATED,
                null,
                detachedApplication,
                detachedActor
        );
        QuestApplication managedApplication = mock(QuestApplication.class);
        AppUser managedActor = new AppUser();
        managedActor.setId(7L);

        when(applicationRepository.findByIdDetailed(42L)).thenReturn(Optional.of(managedApplication));
        when(userRepository.findById(7L)).thenReturn(Optional.of(managedActor));

        handler.handle(event);

        verify(applicationRepository).findByIdDetailed(42L);
        verify(userRepository).findById(7L);
        verify(newsService).notifyApplicationCreated(managedApplication.getQuest(), managedApplication, managedActor, event.eventId());
    }
}
