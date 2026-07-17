package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessGalleryImageRepository extends JpaRepository<BusinessGalleryImage, Long> {

    @Query("""
            select image
            from BusinessGalleryImage image
            join fetch image.businessProfile profile
            join fetch profile.owner owner
            where owner.id = :ownerId
            order by image.sortOrder asc, image.id asc
            """)
    List<BusinessGalleryImage> findByOwnerId(Long ownerId);

    @Query("select image from BusinessGalleryImage image join fetch image.businessProfile profile join fetch profile.owner owner where profile.id = :businessProfileId and owner.id = :ownerId order by image.sortOrder asc, image.id asc")
    List<BusinessGalleryImage> findByBusinessProfileId(Long businessProfileId, Long ownerId);

    @Query("""
            select image
            from BusinessGalleryImage image
            join fetch image.businessProfile profile
            join fetch profile.owner owner
            where image.id = :imageId and owner.id = :ownerId
            """)
    Optional<BusinessGalleryImage> findOwnedById(Long imageId, Long ownerId);

    @Query("""
            select image
            from BusinessGalleryImage image
            join fetch image.businessProfile profile
            where profile.id = :businessProfileId
            and image.active = true
            order by image.sortOrder asc, image.id asc
            """)
    List<BusinessGalleryImage> findActiveByBusinessProfileId(Long businessProfileId);
}
