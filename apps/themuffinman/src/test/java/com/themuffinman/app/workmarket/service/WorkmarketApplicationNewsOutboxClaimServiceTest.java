package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.model.WorkmarketApplicationNewsOutbox;
import com.themuffinman.app.workmarket.repository.WorkmarketApplicationNewsOutboxRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WorkmarketApplicationNewsOutboxClaimServiceTest {

    @Test
    void claimsPendingRowsWithAUniqueLeaseAndOwner() {
        WorkmarketApplicationNewsOutboxRepository repository = mock(WorkmarketApplicationNewsOutboxRepository.class);
        WorkmarketApplicationNewsOutboxClaimService service = new WorkmarketApplicationNewsOutboxClaimService(repository);
        WorkmarketApplicationNewsOutbox item = new WorkmarketApplicationNewsOutbox();
        item.setStatus(WorkmarketApplicationNewsOutbox.Status.PENDING);
        item.setAvailableAt(Instant.now().minusSeconds(1));
        when(repository.findClaimable(any(), eq(WorkmarketApplicationNewsOutbox.Status.PROCESSING), any(Instant.class), any()))
                .thenReturn(List.of(item));

        service.claimNext();

        assertEquals(WorkmarketApplicationNewsOutbox.Status.PROCESSING, item.getStatus());
        org.junit.jupiter.api.Assertions.assertNotNull(item.getLeaseOwner());
        org.junit.jupiter.api.Assertions.assertNotNull(item.getLeaseUntil());
        verify(repository).saveAndFlush(item);
    }
}
