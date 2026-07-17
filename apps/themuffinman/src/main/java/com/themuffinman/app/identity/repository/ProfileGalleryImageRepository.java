package com.themuffinman.app.identity.repository;

import com.themuffinman.app.identity.model.ProfileGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProfileGalleryImageRepository extends JpaRepository<ProfileGalleryImage, Long> {
    List<ProfileGalleryImage> findByOwnerIdAndActiveTrueOrderBySortOrderAscIdAsc(Long ownerId);

    @Query("select image from ProfileGalleryImage image where image.id = :imageId and image.owner.id = :ownerId")
    Optional<ProfileGalleryImage> findOwnedById(Long imageId, Long ownerId);
}
