package com.themuffinman.app.workmarket.repository;

import com.themuffinman.app.workmarket.model.ReviewRole;
import com.themuffinman.app.workmarket.model.UserReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserReviewRepository extends JpaRepository<UserReview, Long> {

    Optional<UserReview> findByQuestIdAndReviewerIdAndReviewedUserId(Long questId, Long reviewerId, Long reviewedUserId);

    @Query("""
            select avg(r.stars) as averageStars, count(r) as reviewCount
            from UserReview r
            where r.reviewedUser.id = :reviewedUserId and r.reviewedRole = :reviewedRole
            """)
    UserRatingSummaryProjection summarizeByReviewedUserIdAndReviewedRole(Long reviewedUserId, ReviewRole reviewedRole);

    @Query("""
            select r
            from UserReview r
            join fetch r.quest q
            join fetch r.reviewer reviewer
            join fetch r.reviewedUser reviewedUser
            where reviewedUser.id = :reviewedUserId
            order by r.createdAt desc
            """)
    List<UserReview> findRecentByReviewedUserId(Long reviewedUserId, Pageable pageable);

    interface UserRatingSummaryProjection {
        Double getAverageStars();
        long getReviewCount();
    }
}
