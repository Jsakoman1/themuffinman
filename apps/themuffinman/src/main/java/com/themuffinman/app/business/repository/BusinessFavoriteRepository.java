package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessFavorite;
import com.themuffinman.app.business.model.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessFavoriteRepository extends JpaRepository<BusinessFavorite, Long> {
    List<BusinessFavorite> findByOwnerIdAndBusinessProfileActiveTrueOrderByCreatedAtDesc(Long ownerId);
    Optional<BusinessFavorite> findByOwnerIdAndBusinessProfileId(Long ownerId, Long businessProfileId);

    @Query("select f.businessProfile from BusinessFavorite f join fetch f.businessProfile.owner where f.owner.id = :ownerId and f.businessProfile.active = true and f.businessProfile.owner.id <> :ownerId order by f.businessProfile.businessName asc, f.businessProfile.id asc")
    List<BusinessProfile> findFavoriteActiveProfiles(Long ownerId);

    @Query("select f.businessProfile from BusinessFavorite f join fetch f.businessProfile.owner where f.owner.id = :ownerId and f.businessProfile.active = true and f.businessProfile.owner.id <> :ownerId and (lower(f.businessProfile.businessName) like lower(concat('%', :query, '%')) or lower(coalesce(f.businessProfile.headline, '')) like lower(concat('%', :query, '%')) or lower(coalesce(f.businessProfile.description, '')) like lower(concat('%', :query, '%'))) order by f.businessProfile.businessName asc, f.businessProfile.id asc")
    List<BusinessProfile> searchFavoriteActiveProfiles(Long ownerId, String query);
}
