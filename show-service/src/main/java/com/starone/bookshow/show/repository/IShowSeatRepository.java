package com.starone.bookshow.show.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.starone.bookshow.show.entity.Show;
import com.starone.bookshow.show.entity.ShowSeat;
import com.starone.common.enums.SeatStatus;

public interface IShowSeatRepository extends JpaRepository<ShowSeat, UUID> {

    // Find all seats for a show
    List<ShowSeat> findByShowId(UUID showId);

    // Find specific seats
    List<ShowSeat> findByShowIdAndSeatNumberIn(UUID showId, List<String> seatNumbers);

    // Count available seats
    long countByShowIdAndStatus(UUID showId, SeatStatus status);

    // Find locked seats that expired (for cleanup job)
    List<ShowSeat> findByStatusAndLockedUntilBefore(SeatStatus status, LocalDateTime time);

    // Bulk update status (for booking confirmation)
    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = :newStatus, ss.bookingId = :bookingId WHERE ss.id IN :seatIds")
    void updateStatusAndBookingId(@Param("seatIds") List<UUID> seatIds,
            @Param("newStatus") SeatStatus newStatus,
            @Param("bookingId") UUID bookingId);

    // Lock seats (temporary hold)
    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'LOCKED', ss.lockedAt = :now, ss.lockedUntil = :expiry, ss.lockedByUserId = :userId "
            +
            "WHERE ss.show.id = :showId AND ss.seatNumber IN :seatNumbers AND ss.status = 'AVAILABLE'")
    int lockSeats(@Param("showId") UUID showId,
            @Param("seatNumbers") List<String> seatNumbers,
            @Param("now") LocalDateTime now,
            @Param("expiry") LocalDateTime expiry,
            @Param("userId") UUID userId);

    //Page<Show> findByScreenIdAndActiveTrueAndShowStartTimeBetween(
     //       UUID screenId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
