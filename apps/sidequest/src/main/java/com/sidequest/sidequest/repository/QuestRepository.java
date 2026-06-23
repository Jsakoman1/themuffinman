package com.sidequest.sidequest.repository;

import com.sidequest.sidequest.model.Quest;
import com.sidequest.sidequest.model.QuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    @Query("select distinct q from Quest q join fetch q.creator left join fetch q.images left join fetch q.visibleToCircles")
    List<Quest> findAllWithCreator();

    @Query("select distinct q from Quest q join fetch q.creator left join fetch q.images left join fetch q.visibleToCircles where q.id = :id")
    Optional<Quest> findByIdWithCreator(Long id);

    long countByCreatorId(Long creatorId);

    long countByCreatorIdAndStatus(Long creatorId, QuestStatus status);

    @Query("select distinct q from Quest q join fetch q.creator left join fetch q.images left join fetch q.visibleToCircles where q.creator.id = :creatorId and q.status = :status order by q.id desc")
    List<Quest> findByCreatorIdAndStatusOrderByIdDesc(Long creatorId, QuestStatus status);
}
