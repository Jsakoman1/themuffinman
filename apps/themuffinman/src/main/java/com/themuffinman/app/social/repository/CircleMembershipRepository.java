package com.themuffinman.app.social.repository;

import com.themuffinman.app.social.model.CircleMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CircleMembershipRepository extends JpaRepository<CircleMembership, Long> {

    @Query("""
            select membership from CircleMembership membership
            join fetch membership.circle circle
            join fetch membership.member member
            where circle.owner.id = :ownerId
            order by circle.name asc, member.username asc
            """)
    List<CircleMembership> findByCircleOwnerId(Long ownerId);

    @Query("""
            select membership from CircleMembership membership
            join fetch membership.circle circle
            join fetch membership.member member
            where member.id = :memberId
              and circle.owner.id = :ownerId
            order by circle.name asc
            """)
    List<CircleMembership> findByMemberIdAndCircleOwnerId(Long memberId, Long ownerId);

    boolean existsByCircleIdAndMemberId(Long circleId, Long memberId);

    void deleteByCircleId(Long circleId);
}
