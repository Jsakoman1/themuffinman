package com.themuffinman.app.rides.repository;

import com.themuffinman.app.rides.model.RideOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RideOfferRepository extends JpaRepository<RideOffer, Long> {
    @Query("""
            select distinct ro from RideOffer ro
            join fetch ro.driver
            left join fetch ro.visibleCircles c
            left join c.memberships m
            where ro.active = true
              and (c is null or ro.driver.id = :viewerId or m.member.id = :viewerId)
            order by ro.departureAt asc, ro.id asc
            """)
    List<RideOffer> findVisibleActiveOffers(Long viewerId);

    @Query("""
            select distinct ro from RideOffer ro
            join fetch ro.driver
            left join fetch ro.visibleCircles
            where ro.driver.id = :driverId
            order by ro.departureAt asc, ro.id asc
            """)
    List<RideOffer> findByDriverId(Long driverId);
}
