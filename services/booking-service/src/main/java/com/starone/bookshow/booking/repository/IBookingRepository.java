package com.starone.bookshow.booking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.starone.bookshow.booking.entity.Booking;
import com.starone.common.enums.BookingStatus;

public interface IBookingRepository extends JpaRepository<Booking, UUID> {

    // Find bookings by user (for "My Bookings")
    Page<Booking> findByUserId(UUID userId, Pageable pageable);

    // Find bookings by show (admin view)
    Page<Booking> findByShowId(UUID showId, Pageable pageable);

    // Find pending bookings for payment reminder / timeout cleanup
    List<Booking> findByStatusAndBookingTimeBefore(BookingStatus status, LocalDateTime time);

    // Find booking by user and status
    Page<Booking> findByUserIdAndStatus(UUID userId, BookingStatus status, Pageable pageable);

    // Find by booking reference (user lookup)
    Optional<Booking> findByBookingReference(String bookingReference);

    // Count bookings per show (for analytics)
    long countByShowId(UUID showId);

    // Find recent bookings
    Page<Booking> findByBookingTimeAfter(LocalDateTime time, Pageable pageable);
}
