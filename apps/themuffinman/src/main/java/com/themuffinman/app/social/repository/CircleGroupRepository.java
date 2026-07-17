package com.themuffinman.app.social.repository;

import com.themuffinman.app.social.model.CircleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CircleGroupRepository extends JpaRepository<CircleGroup, Long> {

    @Query("""
            select distinct c from CircleGroup c
            left join fetch c.memberships membership
            left join fetch membership.member
            where c.owner.id = :ownerId
            order by c.name asc
            """)
    List<CircleGroup> findAllDetailedByOwnerId(Long ownerId);

    @Query("""
            select distinct c from CircleGroup c
            join fetch c.owner
            left join fetch c.memberships membership
            left join fetch membership.member
            order by c.name asc
            """)
    List<CircleGroup> findAllDetailed();

    Optional<CircleGroup> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("""
            select distinct c from CircleGroup c
            join fetch c.owner
            left join fetch c.memberships membership
            left join fetch membership.member
            where c.id = :circleId
              and (c.owner.id = :userId or exists (select membershipCheck.id from CircleMembership membershipCheck where membershipCheck.circle.id = c.id and membershipCheck.member.id = :userId))
            """)
    Optional<CircleGroup> findAccessibleDetailedById(Long circleId, Long userId);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    @Query("""
            select c from CircleGroup c
            where c.owner.id = :ownerId
              and c.id in :ids
            """)
    List<CircleGroup> findAllByOwnerIdAndIdIn(Long ownerId, Collection<Long> ids);
}
