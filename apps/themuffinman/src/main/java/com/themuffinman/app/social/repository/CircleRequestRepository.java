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

    @Query("""
            select c from CircleRequest c
            join fetch c.requester
            join fetch c.recipient
            where (c.requester.id = :leftUserId and c.recipient.id = :rightUserId)
               or (c.requester.id = :rightUserId and c.recipient.id = :leftUserId)
            """)
    Optional<CircleRequest> findBetweenUsers(Long leftUserId, Long rightUserId);

    @Query("""
            select c from CircleRequest c
            join fetch c.requester
            join fetch c.recipient
            left join fetch c.blockedBy
            order by c.createdAt desc
            """)
    List<CircleRequest> findAllDetailed();
}
