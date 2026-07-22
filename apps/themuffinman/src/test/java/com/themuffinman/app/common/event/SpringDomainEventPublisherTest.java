package com.themuffinman.app.common.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SpringDomainEventPublisherTest {

    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final SpringDomainEventPublisher publisher = new SpringDomainEventPublisher(applicationEventPublisher);

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.clear();
    }

    @Test
    void publishesImmediatelyWhenNoTransactionSynchronizationIsActive() {
        DomainEvent event = new TestEvent();

        publisher.publish(event);

        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    void defersPublicationUntilAfterCommitWhenSynchronizationIsActive() {
        TransactionSynchronizationManager.initSynchronization();
        DomainEvent event = new TestEvent();

        publisher.publish(event);

        verifyNoInteractions(applicationEventPublisher);
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(applicationEventPublisher).publishEvent(event);
    }

    private record TestEvent() implements DomainEvent {
    }
}
