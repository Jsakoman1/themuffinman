package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    @Query("select bp from BusinessProfile bp join fetch bp.owner where bp.owner.id = :ownerId")
    Optional<BusinessProfile> findByOwnerId(Long ownerId);

    @Query("select bp from BusinessProfile bp join fetch bp.owner where bp.slug = :slug")
    Optional<BusinessProfile> findBySlug(String slug);

    @Query("select bp from BusinessProfile bp join fetch bp.owner where bp.active = true order by bp.businessName asc, bp.id asc")
    List<BusinessProfile> findActiveProfiles();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndOwnerIdNot(String slug, Long ownerId);
}
