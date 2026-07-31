package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusinessReviewRepository extends JpaRepository<BusinessReview, Long> {

    Optional<BusinessReview> findByBusinessBookingIdAndReviewerId(Long bookingId, Long reviewerId);

    @Query("""
            select review
            from BusinessReview review
            join fetch review.reviewer reviewer
            where review.businessProfile.id = :businessProfileId
            order by review.createdAt desc, review.id desc
            """)
    List<BusinessReview> findPublicByBusinessProfileId(Long businessProfileId, Pageable pageable);

    @Query("""
            select avg(review.stars) as averageStars, count(review) as reviewCount
            from BusinessReview review
            where review.businessProfile.id = :businessProfileId
            """)
    BusinessRatingSummaryProjection summarizeByBusinessProfileId(Long businessProfileId);

    interface BusinessRatingSummaryProjection {
        Double getAverageStars();
        long getReviewCount();
    }
}
