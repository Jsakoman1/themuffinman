package com.themuffinman.app.vision.repository;

import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    @Query("select distinct q from Quest q join fetch q.creator left join fetch q.images left join fetch q.visibleToCircles")
    List<Quest> findForQuestList();

    @Query("select distinct q from Quest q join fetch q.creator left join fetch q.images left join fetch q.visibleToCircles where q.id in :ids")
    List<Quest> findForQuestListByIds(@Param("ids") List<Long> ids);

    @Query("select distinct q from Quest q join fetch q.creator left join fetch q.images left join fetch q.visibleToCircles where q.id = :id")
    Optional<Quest> findForQuestDetail(Long id);

    @Query(value = """
            select q.id
            from quest q
            where q.location_latitude is not null
              and q.location_longitude is not null
              and ST_DWithin(
                    ST_SetSRID(ST_MakePoint(q.location_longitude, q.location_latitude), 4326)::geography,
                    ST_SetSRID(ST_MakePoint(:originLongitude, :originLatitude), 4326)::geography,
                    :radiusMeters
              )
            """, nativeQuery = true)
    List<Long> findIdsWithinRadius(
            @Param("originLatitude") BigDecimal originLatitude,
            @Param("originLongitude") BigDecimal originLongitude,
            @Param("radiusMeters") int radiusMeters
    );

    long countByCreatorId(Long creatorId);

    long countByCreatorIdAndStatus(Long creatorId, QuestStatus status);

    long countByLocationLatitudeIsNotNullAndLocationLongitudeIsNotNull();

    long countByLocationProviderPlaceIdIsNotNull();

    @Query("select distinct q from Quest q join fetch q.creator left join fetch q.images left join fetch q.visibleToCircles where q.creator.id = :creatorId and q.status = :status order by q.id desc")
    List<Quest> findForOwnerStatusList(Long creatorId, QuestStatus status);

    default List<Quest> findAllWithCreator() {
        return findForQuestList();
    }

    default List<Quest> findAllWithCreatorByIds(List<Long> ids) {
        return findForQuestListByIds(ids);
    }

    default Optional<Quest> findByIdWithCreator(Long id) {
        return findForQuestDetail(id);
    }

    default List<Quest> findByCreatorIdAndStatusOrderByIdDesc(Long creatorId, QuestStatus status) {
        return findForOwnerStatusList(creatorId, status);
    }
}
