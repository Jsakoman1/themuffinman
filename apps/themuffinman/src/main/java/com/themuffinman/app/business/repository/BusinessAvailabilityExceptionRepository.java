package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessAvailabilityException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessAvailabilityExceptionRepository extends JpaRepository<BusinessAvailabilityException, Long> {

    @Query("""
            select exceptionRow
            from BusinessAvailabilityException exceptionRow
            join fetch exceptionRow.businessProfile profile
            join fetch profile.owner owner
            left join fetch exceptionRow.businessOffering offering
            where owner.id = :ownerId
            order by exceptionRow.startAt asc, exceptionRow.id asc
            """)
    List<BusinessAvailabilityException> findByOwnerId(Long ownerId);

    @Query("""
            select exceptionRow
            from BusinessAvailabilityException exceptionRow
            join fetch exceptionRow.businessProfile profile
            join fetch profile.owner owner
            left join fetch exceptionRow.businessOffering offering
            where exceptionRow.id = :exceptionId and owner.id = :ownerId
            """)
    Optional<BusinessAvailabilityException> findOwnedById(Long exceptionId, Long ownerId);

    @Query("""
            select exceptionRow
            from BusinessAvailabilityException exceptionRow
            join fetch exceptionRow.businessProfile profile
            left join fetch exceptionRow.businessOffering offering
            where profile.id = :businessProfileId
            order by exceptionRow.startAt asc, exceptionRow.id asc
            """)
    List<BusinessAvailabilityException> findByBusinessProfileId(Long businessProfileId);

    @Query("""
            select exceptionRow
            from BusinessAvailabilityException exceptionRow
            join fetch exceptionRow.businessProfile profile
            left join fetch exceptionRow.businessOffering offering
            where profile.slug = :slug
            and profile.active = true
            order by exceptionRow.startAt asc, exceptionRow.id asc
            """)
    List<BusinessAvailabilityException> findByBusinessSlug(String slug);
}
