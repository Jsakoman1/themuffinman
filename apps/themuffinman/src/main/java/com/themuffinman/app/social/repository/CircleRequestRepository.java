package com.themuffinman.app.social.repository;

import com.themuffinman.app.social.model.CircleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CircleRequestRepository extends JpaRepository<CircleRequest, Long> {

    @Query("""
            select c from CircleRequest c
            join fetch c.requester
            join fetch c.recipient
            where (c.requester.id = :userId or c.recipient.id = :userId)
              and c.acceptedAt is not null
            order by c.acceptedAt desc, c.createdAt desc
            """)
    List<CircleRequest> findAcceptedByUserId(Long userId);

    @Query("""
            select c from CircleRequest c
            join fetch c.requester
            join fetch c.recipient
            where c.recipient.id = :userId
              and c.acceptedAt is null
              and c.blockedAt is null
            order by c.createdAt desc
            """)
    List<CircleRequest> findIncomingPendingByRecipientId(Long userId);

    @Query("""
            select c from CircleRequest c
            join fetch c.requester
            join fetch c.recipient
            where c.requester.id = :userId
              and c.acceptedAt is null
              and c.blockedAt is null
            order by c.createdAt desc
            """)
    List<CircleRequest> findOutgoingPendingByRequesterId(Long userId);

    @Query(value = """
            select c.* from circle_request c
            where (c.requester_id = :leftUserId and c.recipient_id = :rightUserId)
               or (c.requester_id = :rightUserId and c.recipient_id = :leftUserId)
            order by c.created_at desc, c.id desc
            limit 1
            """, nativeQuery = true)
    Optional<CircleRequest> findBetweenUsers(Long leftUserId, Long rightUserId);

    @Query("""
            select c from CircleRequest c
            join fetch c.requester
            join fetch c.recipient
            left join fetch c.blockedBy
            where c.blockedAt is not null
              and c.blockedBy.id = :userId
            order by c.blockedAt desc, c.createdAt desc
            """)
    List<CircleRequest> findBlockedByUserId(Long userId);

    @Query("""
            select c from CircleRequest c
            join fetch c.requester
            join fetch c.recipient
            left join fetch c.blockedBy
            order by c.createdAt desc
            """)
    List<CircleRequest> findAllDetailed();
}
