package com.themuffinman.app.location.repository;

import com.themuffinman.app.location.model.LocationLookupEvent;
import com.themuffinman.app.location.model.LocationLookupEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface LocationLookupEventRepository extends JpaRepository<LocationLookupEvent, Long> {
    long countByCreatedAtGreaterThanEqual(Instant createdAt);
    long countByCreatedAtGreaterThanEqualAndRequestType(Instant createdAt, LocationLookupEventType requestType);
}
