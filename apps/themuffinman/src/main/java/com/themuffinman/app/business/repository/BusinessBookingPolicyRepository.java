package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessBookingPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BusinessBookingPolicyRepository extends JpaRepository<BusinessBookingPolicy, Long> {

    @Query("""
            select policy
            from BusinessBookingPolicy policy
            join fetch policy.businessProfile profile
            join fetch profile.owner owner
            where owner.id = :ownerId
            """)
    Optional<BusinessBookingPolicy> findByOwnerId(Long ownerId);
}
