package com.starone.bookshow.show.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.starone.bookshow.show.entity.Show;

public interface IShowRepository extends JpaRepository<Show, UUID> {
    // Find shows by movie ID (for movie details page)
    List<Show> findByMovieId(UUID movieId);

    // Paginated shows by movie
    Page<Show> findByMovieId(UUID movieId, Pageable pageable);

    // Find shows by screen ID (for theater admin)
    List<Show> findByScreenId(UUID screenId);

    // Find active shows for a screen on a specific date
    Page<Show> findByScreenIdAndActiveTrueAndShowStartTimeBetween(
            UUID screenId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Find upcoming shows (active and start time > now)
    Page<Show> findByActiveTrueAndShowStartTimeAfter(LocalDateTime now, Pageable pageable);

    // Find shows running today (for home page / now showing)
    @Query("SELECT s FROM Show s WHERE s.active = true AND s.showStartTime >= :start AND s.showStartTime < :end")
    Page<Show> findTodayShows(@Param("start") LocalDateTime dayStart, @Param("end") LocalDateTime dayEnd,
            Pageable pageable);

    // Find shows by theater (via screen)
    @Query("""
                SELECT s
                FROM Show s
                WHERE s.theaterId = :theaterId
                  AND s.active = true
            """)
    Page<Show> findByTheaterId(@Param("theaterId") UUID theaterId, Pageable pageable);

    // Check for overlapping shows on same screen
    boolean existsByScreenIdAndShowStartTimeBetween(UUID screenId, LocalDateTime start, LocalDateTime end);
}
