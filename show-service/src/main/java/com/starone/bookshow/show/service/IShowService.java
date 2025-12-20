package com.starone.bookshow.show.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.show.dto.ShowRequestDto;
import com.starone.bookshow.show.dto.ShowResponseDto;
import com.starone.bookshow.show.dto.ShowSeatResponseDto;

public interface IShowService {
    /**
     * Create a new show (admin) - auto-generates ShowSeats from screen layout
     */
    ShowResponseDto createShow(ShowRequestDto requestDto);

    /**
     * Get show by ID with full enrichment (movie, theater, screen details)
     */
    ShowResponseDto getShowById(UUID showId);

    /**
     * Update show (start time, pricing, type, formats)
     */
    ShowResponseDto updateShow(UUID showId, ShowRequestDto requestDto);

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
    Page<ShowResponseDto> getShowsByMovieId(UUID movieId, Pageable pageable);

    /**
     * Get all shows for a screen/theater on a date
     */
    Page<ShowResponseDto> getShowsByScreenAndDate(UUID screenId, LocalDateTime date, Pageable pageable);

    /**
     * Get today's shows (active)
     */
    Page<ShowResponseDto> getTodayShows(Pageable pageable);

    /**
     * Get upcoming shows (active and future)
     */
    Page<ShowResponseDto> getUpcomingShows(Pageable pageable);

    /**
     * Lock seats for temporary hold (10 min)
     */
    List<ShowSeatResponseDto> lockSeats(UUID showId, List<String> seatNumbers, UUID userId);

    /**
     * Release expired locks (cleanup job calls this)
     */
    int releaseExpiredLocks(LocalDateTime expiryTime);

    /**
     * Check seat availability (for frontend validation)
     */
    boolean areSeatsAvailable(UUID showId, List<String> seatNumbers);
}
