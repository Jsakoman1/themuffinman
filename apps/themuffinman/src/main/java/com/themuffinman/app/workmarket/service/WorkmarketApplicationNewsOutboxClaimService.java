package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.model.WorkmarketApplicationNewsOutbox;
import com.themuffinman.app.workmarket.repository.WorkmarketApplicationNewsOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkmarketApplicationNewsOutboxClaimService {

    private static final long LEASE_SECONDS = 60;

    private final WorkmarketApplicationNewsOutboxRepository outboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<WorkmarketApplicationNewsOutbox> claimNext() {
        Instant now = Instant.now();
        List<WorkmarketApplicationNewsOutbox> items = outboxRepository.findClaimable(
                List.of(WorkmarketApplicationNewsOutbox.Status.PENDING, WorkmarketApplicationNewsOutbox.Status.FAILED),
                WorkmarketApplicationNewsOutbox.Status.PROCESSING,
                now,
                PageRequest.of(0, 1)
        );
        if (items.isEmpty()) {
            return Optional.empty();
        }

        WorkmarketApplicationNewsOutbox item = items.getFirst();
        item.setStatus(WorkmarketApplicationNewsOutbox.Status.PROCESSING);
        item.setLeaseOwner("workmarket-news-outbox:" + UUID.randomUUID());
        item.setLeaseUntil(now.plusSeconds(LEASE_SECONDS));
        item.setUpdatedAt(now);
        outboxRepository.saveAndFlush(item);
        return Optional.of(item);
    }
}
