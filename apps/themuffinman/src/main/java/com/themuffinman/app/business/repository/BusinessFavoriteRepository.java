package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessFavoriteRepository extends JpaRepository<BusinessFavorite, Long> {
    List<BusinessFavorite> findByOwnerIdAndBusinessProfileActiveTrueOrderByCreatedAtDesc(Long ownerId);
    Optional<BusinessFavorite> findByOwnerIdAndBusinessProfileId(Long ownerId, Long businessProfileId);
}
