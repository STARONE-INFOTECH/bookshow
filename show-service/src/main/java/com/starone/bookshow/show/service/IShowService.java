package com.starone.bookshow.show.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.show.dto.ShowRequestDto;
import com.starone.common.response.record.ShowResponse;
import com.starone.common.response.record.ShowSeatResponse;

public interface IShowService {
    /**
     * Create a new show (admin) - auto-generates ShowSeats from screen layout
     */
    ShowResponse createShow(ShowRequestDto requestDto);

    /**
     * Get show by ID with full enrichment (movie, theater, screen details)
     */
    ShowResponse getShowById(UUID showId);

    /**
     * Update show (start time, pricing, type, formats)
     */
    ShowResponse updateShow(UUID showId, ShowRequestDto requestDto);

    /**
     * Deactivate show (cancel screening)
     */
    void deactivateShow(UUID showId);

    /**
     * Activate show
     */
    void activateShow(UUID showId);

    /**
     * Get all shows for a movie (paginated)
     */
    Page<ShowResponse> getShowsByMovieId(UUID movieId, Pageable pageable);

    /**
     * Get all shows for a screen/theater on a date
     */
    Page<ShowResponse> getShowsByScreenAndDate(UUID screenId, LocalDateTime date, Pageable pageable);

    /**
     * Get today's shows (active)
     */
    Page<ShowResponse> getTodayShows(Pageable pageable);

    /**
     * Get upcoming shows (active and future)
     */
    Page<ShowResponse> getUpcomingShows(Pageable pageable);

    /**
     * Lock seats for temporary hold (10 min)
     */
    List<ShowSeatResponse> lockSeats(UUID showId, List<String> seatNumbers, UUID userId);

    /**
     * Release expired locks (cleanup job calls this)
     */
    int releaseExpiredLocks(LocalDateTime expiryTime);

    /**
     * Check seat availability (for frontend validation)
     */
    boolean areSeatsAvailable(UUID showId, List<String> seatNumbers);
}
