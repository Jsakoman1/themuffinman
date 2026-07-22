package com.themuffinman.app.workmarket.repository;

import com.themuffinman.app.workmarket.model.WorkmarketApplicationNewsOutbox;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WorkmarketApplicationNewsOutboxRepository extends JpaRepository<WorkmarketApplicationNewsOutbox, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from WorkmarketApplicationNewsOutbox item where ((item.status in :retryableStatuses and item.availableAt <= :now) or (item.status = :processingStatus and (item.leaseUntil is null or item.leaseUntil <= :now))) order by item.createdAt asc")
    List<WorkmarketApplicationNewsOutbox> findClaimable(
            @Param("retryableStatuses") List<WorkmarketApplicationNewsOutbox.Status> retryableStatuses,
            @Param("processingStatus") WorkmarketApplicationNewsOutbox.Status processingStatus,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Modifying
    @Query("delete from WorkmarketApplicationNewsOutbox item where item.status = com.themuffinman.app.workmarket.model.WorkmarketApplicationNewsOutbox$Status.DELIVERED and item.updatedAt < :cutoff")
    int deleteDeliveredBefore(@Param("cutoff") Instant cutoff);
}
