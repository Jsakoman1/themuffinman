package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessOffering;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessOfferingRepository extends JpaRepository<BusinessOffering, Long> {

    @Query("""
            select offering
            from BusinessOffering offering
            join fetch offering.businessProfile profile
            join fetch profile.owner owner
            where owner.id = :ownerId
            order by offering.sortOrder asc, offering.title asc, offering.id asc
            """)
    List<BusinessOffering> findByOwnerId(Long ownerId);

    @Query("""
            select offering
            from BusinessOffering offering
            join fetch offering.businessProfile profile
            join fetch profile.owner owner
            where offering.id = :offeringId and owner.id = :ownerId
            """)
    Optional<BusinessOffering> findOwnedById(Long offeringId, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select offering
            from BusinessOffering offering
            join fetch offering.businessProfile profile
            join fetch profile.owner owner
            where offering.id = :offeringId
            """)
    Optional<BusinessOffering> findByIdForUpdate(Long offeringId);

    @Query("""
            select offering
            from BusinessOffering offering
            join fetch offering.businessProfile profile
            where profile.id = :businessProfileId
            and offering.active = true
            order by offering.sortOrder asc, offering.title asc, offering.id asc
            """)
    List<BusinessOffering> findActiveByBusinessProfileId(Long businessProfileId);

    boolean existsByBusinessProfileIdAndSlug(Long businessProfileId, String slug);

    boolean existsByBusinessProfileIdAndSlugAndIdNot(Long businessProfileId, String slug, Long id);
}
