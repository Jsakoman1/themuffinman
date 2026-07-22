package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.config.RetentionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEvent;
import com.themuffinman.app.workmarket.event.WorkmarketQuestApplicationNewsEventHandler;
import com.themuffinman.app.workmarket.model.WorkmarketApplicationNewsOutbox;
import com.themuffinman.app.workmarket.model.WorkmarketQuestApplicationNewsEventType;
import com.themuffinman.app.workmarket.repository.WorkmarketApplicationNewsOutboxRepository;
import com.themuffinman.app.workmarket.model.QuestApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkmarketApplicationNewsOutboxService implements WorkmarketApplicationNewsPublisher {

    private final WorkmarketApplicationNewsOutboxRepository outboxRepository;
    private final WorkmarketQuestApplicationNewsEventHandler eventHandler;
    private final WorkmarketApplicationNewsOutboxClaimService claimService;
    private final PlatformTransactionManager transactionManager;
    private final RetentionProperties retentionProperties;

    @Override
    @Transactional
    public void publish(WorkmarketQuestApplicationNewsEvent.Type type, QuestApplication application, AppUser actor) {
        WorkmarketApplicationNewsOutbox item = new WorkmarketApplicationNewsOutbox();
        item.setEventId(UUID.randomUUID());
        item.setEventType(WorkmarketQuestApplicationNewsEventType.valueOf(type.name()));
        item.setApplicationId(application.getId());
        item.setActorUserId(actor.getId());
        item.setStatus(WorkmarketApplicationNewsOutbox.Status.PENDING);
        item.setAvailableAt(Instant.now());
        item.setCreatedAt(Instant.now());
        outboxRepository.save(item);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    new org.springframework.transaction.support.TransactionTemplate(transactionManager)
                            .executeWithoutResult(status -> dispatchOneInternal());
                }
            });
        }
    }

    @Scheduled(fixedDelayString = "${app.side-effects.workmarket-news-outbox.poll-ms:5000}")
    public void dispatchScheduled() {
        dispatchOne();
    }

    @Transactional
    public boolean dispatchOne() {
        return dispatchOneInternal();
    }

    @Transactional
    public boolean requestOperatorReplay(UUID eventId, String operator) {
        Optional<WorkmarketApplicationNewsOutbox> found = outboxRepository.findById(eventId);
        if (found.isEmpty()) {
            return false;
        }
        WorkmarketApplicationNewsOutbox item = found.get();
        item.setStatus(WorkmarketApplicationNewsOutbox.Status.PENDING);
        item.setAvailableAt(Instant.now());
        item.setLeaseOwner(null);
        item.setLeaseUntil(null);
        item.setFailureCode(null);
        item.setLastError(null);
        item.setReplayReference("operator:" + (operator == null || operator.isBlank() ? "unknown" : operator) + ":" + eventId);
        item.setUpdatedAt(Instant.now());
        outboxRepository.save(item);
        return true;
    }

    @Scheduled(cron = "${app.retention.notifications.cleanup-cron:0 30 3 * * *}")
    @Transactional
    public int deleteExpiredDeliveredItems() {
        int retentionDays = Math.max(retentionProperties.getNotifications().getDays(), 1);
        return outboxRepository.deleteDeliveredBefore(TimeSupport.daysAgo(retentionDays));
    }

    private boolean dispatchOneInternal() {
        Optional<WorkmarketApplicationNewsOutbox> claimed = claimService.claimNext();
        if (claimed.isEmpty()) {
            return false;
        }

        WorkmarketApplicationNewsOutbox item = claimed.get();
        try {
            QuestApplication application = new QuestApplication();
            application.setId(item.getApplicationId());
            AppUser actor = new AppUser();
            actor.setId(item.getActorUserId());
            eventHandler.handle(new WorkmarketQuestApplicationNewsEvent(
                    WorkmarketQuestApplicationNewsEvent.Type.valueOf(item.getEventType().name()),
                    null,
                    application,
                    actor,
                    item.getEventId()
            ));
            item.setStatus(WorkmarketApplicationNewsOutbox.Status.DELIVERED);
            item.setDeliveredAt(Instant.now());
            item.setLastError(null);
            item.setFailureCode(null);
            item.setReplayReference(null);
        } catch (RuntimeException failure) {
            item.setStatus(WorkmarketApplicationNewsOutbox.Status.FAILED);
            item.setAttempts(item.getAttempts() + 1);
            item.setAvailableAt(Instant.now().plusSeconds(Math.min(300, 5L << Math.min(item.getAttempts(), 6))));
            item.setLastError(safeMessage(failure));
            item.setFailureCode("NEWS_CONSUMER_FAILURE");
            item.setReplayReference("automatic-retry:" + item.getEventId());
        }
        item.setLeaseOwner(null);
        item.setLeaseUntil(null);
        item.setUpdatedAt(Instant.now());
        outboxRepository.save(item);
        return true;
    }

    private String safeMessage(RuntimeException failure) {
        String message = failure.getMessage();
        return message == null ? failure.getClass().getSimpleName() : message.substring(0, Math.min(message.length(), 1000));
    }
}
