package com.starone.bookshow.show.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.starone.springcommon.response.record.ShowSeatResponse;

public interface IShowSeatService {
    /**
     * Lock seats for a user (temporary hold - 10 minutes)
     * Used during booking flow
     */
    List<ShowSeatResponse> lockSeats(UUID showId, List<String> seatNumbers, UUID userId);

    /**
     * Release seats (manual or on timeout)
     */
    void releaseSeats(UUID showId, List<String> seatNumbers);

    /**
     * Mark seats as booked (after payment success)
     */
    void bookSeats(UUID showId, List<String> seatNumbers, UUID bookingId);

    /**
     * Get current status of specific seats
     */
    List<ShowSeatResponse> getSeatStatus(UUID showId, List<String> seatNumbers);

    /**
     * Get all seats for a show (for admin or seat map with status)
     */
    List<ShowSeatResponse> getAllSeatsForShow(UUID showId);

    /**
     * Release all expired locks (called by scheduled job)
     */
    int releaseExpiredLocks(LocalDateTime expiryTime);

    /**
     * Check if seats are available (for validation before lock)
     */
    boolean areSeatsAvailable(UUID showId, List<String> seatNumbers);
}
