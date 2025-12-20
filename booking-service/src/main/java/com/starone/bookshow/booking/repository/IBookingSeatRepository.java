package com.starone.bookshow.booking.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.starone.bookshow.booking.entity.BookingSeat;

public interface IBookingSeatRepository extends JpaRepository<BookingSeat, UUID> {

    // Find all seats for a booking (ticket details)
    List<BookingSeat> findByBookingId(UUID bookingId);

    // Find by showSeatId (for cleanup on cancellation)
    List<BookingSeat> findByShowSeatIdIn(List<UUID> showSeatIds);

    // Optional: find by show
    List<BookingSeat> findByBooking_ShowId(UUID showId);
}