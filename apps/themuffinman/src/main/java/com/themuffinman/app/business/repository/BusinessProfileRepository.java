package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    @Query(value = "select bp.* from business_profile bp where bp.owner_id = :ownerId order by bp.active desc, bp.business_name asc, bp.id asc limit 1", nativeQuery = true)
    Optional<BusinessProfile> findByOwnerId(Long ownerId);

    @Query("select bp from BusinessProfile bp join fetch bp.owner where bp.owner.id = :ownerId order by bp.active desc, bp.businessName asc, bp.id asc")
    List<BusinessProfile> findAllByOwnerId(Long ownerId);

    @Query("select bp from BusinessProfile bp join fetch bp.owner where bp.slug = :slug")
    Optional<BusinessProfile> findBySlug(String slug);

    @Query("select bp from BusinessProfile bp join fetch bp.owner where bp.active = true order by bp.businessName asc, bp.id asc")
    List<BusinessProfile> findActiveProfiles();

    @Query("select bp from BusinessProfile bp join fetch bp.owner where bp.active = true and (lower(bp.businessName) like lower(concat('%', :query, '%')) or lower(coalesce(bp.headline, '')) like lower(concat('%', :query, '%')) or lower(coalesce(bp.description, '')) like lower(concat('%', :query, '%'))) order by bp.businessName asc, bp.id asc")
    List<BusinessProfile> searchActiveProfiles(String query);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndOwnerIdNot(String slug, Long ownerId);
}
