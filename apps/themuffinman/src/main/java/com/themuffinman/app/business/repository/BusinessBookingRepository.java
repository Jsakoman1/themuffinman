package com.themuffinman.app.business.repository;

import com.themuffinman.app.business.model.BusinessBooking;
import com.themuffinman.app.business.model.BusinessBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BusinessBookingRepository extends JpaRepository<BusinessBooking, Long> {

    @Query("""
            select booking
            from BusinessBooking booking
            join fetch booking.businessProfile profile
            join fetch profile.owner owner
            join fetch booking.businessOffering offering
            join fetch booking.customerUser customer
            where booking.id = :bookingId
            """)
    Optional<BusinessBooking> findDetailedById(Long bookingId);

    @Query("""
            select booking
            from BusinessBooking booking
            join fetch booking.businessProfile profile
            join fetch profile.owner owner
            join fetch booking.businessOffering offering
            join fetch booking.customerUser customer
            where customer.id = :customerUserId
            order by booking.startsAt desc, booking.id desc
            """)
    List<BusinessBooking> findDetailedByCustomerUserId(Long customerUserId);

    @Query("""
            select booking
            from BusinessBooking booking
            join fetch booking.businessProfile profile
            join fetch profile.owner owner
            join fetch booking.businessOffering offering
            join fetch booking.customerUser customer
            where owner.id = :ownerId
            order by booking.startsAt desc, booking.id desc
            """)
    List<BusinessBooking> findDetailedByOwnerId(Long ownerId);

    @Query("""
            select booking
            from BusinessBooking booking
            join fetch booking.businessProfile profile
            join fetch profile.owner owner
            join fetch booking.businessOffering offering
            join fetch booking.customerUser customer
            where booking.id = :bookingId and customer.id = :customerUserId
            """)
    Optional<BusinessBooking> findDetailedByIdAndCustomerUserId(Long bookingId, Long customerUserId);

    @Query("""
            select booking
            from BusinessBooking booking
            join fetch booking.businessProfile profile
            join fetch profile.owner owner
            join fetch booking.businessOffering offering
            join fetch booking.customerUser customer
            where booking.id = :bookingId and owner.id = :ownerId
            """)
    Optional<BusinessBooking> findDetailedByIdAndOwnerId(Long bookingId, Long ownerId);

    Optional<BusinessBooking> findByCustomerUserIdAndIdempotencyKey(Long customerUserId, String idempotencyKey);

    Optional<BusinessBooking> findByBusinessProfileOwnerIdAndIdempotencyKey(Long ownerId, String idempotencyKey);

    @Query("""
            select count(booking)
            from BusinessBooking booking
            where booking.businessOffering.id = :offeringId
            and booking.status in :statuses
            and booking.startsAt < :endsAt
            and booking.endsAt > :startsAt
            """)
    long countOverlappingBookings(Long offeringId, Collection<BusinessBookingStatus> statuses, Instant startsAt, Instant endsAt);

    @Query("""
            select count(booking)
            from BusinessBooking booking
            where booking.businessOffering.id = :offeringId
            and booking.id <> :excludedBookingId
            and booking.status in :statuses
            and booking.startsAt < :endsAt
            and booking.endsAt > :startsAt
            """)
    long countOverlappingBookingsExcluding(Long offeringId, Long excludedBookingId, Collection<BusinessBookingStatus> statuses, Instant startsAt, Instant endsAt);

    @Query("""
            select booking
            from BusinessBooking booking
            join fetch booking.businessProfile profile
            join fetch profile.owner owner
            join fetch booking.businessOffering offering
            join fetch booking.customerUser customer
            where owner.id = :ownerId
            and booking.startsAt >= :from
            and booking.startsAt < :to
            order by booking.startsAt asc, booking.id asc
            """)
    List<BusinessBooking> findDetailedByOwnerIdAndStartsAtBetween(Long ownerId, Instant from, Instant to);

    @Query("""
            select booking
            from BusinessBooking booking
            join fetch booking.businessProfile profile
            join fetch profile.owner owner
            join fetch booking.businessOffering offering
            join fetch booking.customerUser customer
            where owner.id = :ownerId
            and booking.status in :statuses
            and booking.endsAt >= :now
            order by booking.startsAt asc, booking.id asc
            """)
    List<BusinessBooking> findUpcomingDetailedByOwnerId(Long ownerId, Collection<BusinessBookingStatus> statuses, Instant now);

    @Query("""
            select booking
            from BusinessBooking booking
            join fetch booking.businessProfile profile
            join fetch profile.owner owner
            join fetch booking.businessOffering offering
            join fetch booking.customerUser customer
            where customer.id = :customerUserId
            and booking.status in :statuses
            and booking.endsAt >= :now
            order by booking.startsAt asc, booking.id asc
            """)
    List<BusinessBooking> findUpcomingDetailedByCustomerUserId(Long customerUserId, Collection<BusinessBookingStatus> statuses, Instant now);
}
