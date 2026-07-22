package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.config.RetentionProperties;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEventHandler;
import com.themuffinman.app.workmarket.model.WorkmarketApplicationNewsOutbox;
import com.themuffinman.app.workmarket.repository.WorkmarketApplicationNewsOutboxRepository;
import com.themuffinman.app.workmarket.model.QuestApplication;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkmarketApplicationNewsOutboxServiceTest {

    @Test
    void publishesOnlyIdentifiersIntoTheDurableBoundary() {
        WorkmarketApplicationNewsOutboxRepository repository = mock(WorkmarketApplicationNewsOutboxRepository.class);
        WorkmarketQuestApplicationNewsEventHandler handler = mock(WorkmarketQuestApplicationNewsEventHandler.class);
        WorkmarketApplicationNewsOutboxService service = service(repository, handler, mock(WorkmarketApplicationNewsOutboxClaimService.class));
        QuestApplication application = new QuestApplication();
        application.setId(42L);
        AppUser actor = new AppUser();
        actor.setId(7L);

        service.publish(WorkmarketQuestApplicationNewsEvent.Type.CREATED, application, actor);

        verify(repository).save(argThat(item -> item.getApplicationId().equals(42L)
                && item.getActorUserId().equals(7L)
                && item.getStatus() == WorkmarketApplicationNewsOutbox.Status.PENDING
                && item.getEventId() != null));
    }

    @Test
    void marksDeliveryFailedAndSchedulesRetryWhenConsumerFails() {
        WorkmarketApplicationNewsOutboxRepository repository = mock(WorkmarketApplicationNewsOutboxRepository.class);
        WorkmarketQuestApplicationNewsEventHandler handler = mock(WorkmarketQuestApplicationNewsEventHandler.class);
        WorkmarketApplicationNewsOutboxClaimService claimService = mock(WorkmarketApplicationNewsOutboxClaimService.class);
        WorkmarketApplicationNewsOutboxService service = service(repository, handler, claimService);
        WorkmarketApplicationNewsOutbox item = item();
        when(claimService.claimNext()).thenReturn(java.util.Optional.of(item));
        doThrow(new IllegalStateException("temporary consumer failure"))
                .when(handler).handle(any(WorkmarketQuestApplicationNewsEvent.class));

        assertEquals(true, service.dispatchOne());

        assertEquals(WorkmarketApplicationNewsOutbox.Status.FAILED, item.getStatus());
        assertEquals(1, item.getAttempts());
        verify(repository).save(item);
    }

    @Test
    void operatorReplayClearsFailureAndRecordsReplayReference() {
        WorkmarketApplicationNewsOutboxRepository repository = mock(WorkmarketApplicationNewsOutboxRepository.class);
        WorkmarketQuestApplicationNewsEventHandler handler = mock(WorkmarketQuestApplicationNewsEventHandler.class);
        WorkmarketApplicationNewsOutboxService service = service(repository, handler, mock(WorkmarketApplicationNewsOutboxClaimService.class));
        WorkmarketApplicationNewsOutbox item = item();
        item.setStatus(WorkmarketApplicationNewsOutbox.Status.FAILED);
        item.setFailureCode("NEWS_CONSUMER_FAILURE");
        item.setLastError("temporary failure");
        when(repository.findById(item.getEventId())).thenReturn(java.util.Optional.of(item));

        assertEquals(true, service.requestOperatorReplay(item.getEventId(), "admin"));

        assertEquals(WorkmarketApplicationNewsOutbox.Status.PENDING, item.getStatus());
        assertEquals("operator:admin:" + item.getEventId(), item.getReplayReference());
        verify(repository).save(item);
    }

    @Test
    void deletesOnlyDeliveredItemsPastConfiguredRetention() {
        WorkmarketApplicationNewsOutboxRepository repository = mock(WorkmarketApplicationNewsOutboxRepository.class);
        RetentionProperties retention = new RetentionProperties();
        retention.getNotifications().setDays(14);
        WorkmarketApplicationNewsOutboxService service = new WorkmarketApplicationNewsOutboxService(
                repository,
                mock(WorkmarketQuestApplicationNewsEventHandler.class),
                mock(WorkmarketApplicationNewsOutboxClaimService.class),
                mock(PlatformTransactionManager.class),
                retention,
                new AgentProperties()
        );

        service.deleteExpiredDeliveredItems();

        verify(repository).deleteDeliveredBefore(any(Instant.class));
    }

    private WorkmarketApplicationNewsOutboxService service(
            WorkmarketApplicationNewsOutboxRepository repository,
            WorkmarketQuestApplicationNewsEventHandler handler,
            WorkmarketApplicationNewsOutboxClaimService claimService
    ) {
        return new WorkmarketApplicationNewsOutboxService(
                repository,
                handler,
                claimService,
                mock(PlatformTransactionManager.class),
                new RetentionProperties(),
                new AgentProperties()
        );
    }

    private WorkmarketApplicationNewsOutbox item() {
        WorkmarketApplicationNewsOutbox item = new WorkmarketApplicationNewsOutbox();
        item.setEventId(UUID.randomUUID());
        item.setEventType(com.themuffinman.app.workmarket.model.WorkmarketQuestApplicationNewsEventType.CREATED);
        item.setApplicationId(42L);
        item.setActorUserId(7L);
        item.setStatus(WorkmarketApplicationNewsOutbox.Status.PENDING);
        item.setAvailableAt(Instant.now());
        item.setCreatedAt(Instant.now());
        return item;
    }
}
