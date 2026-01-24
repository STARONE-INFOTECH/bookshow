package com.starone.bookshow.movie.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.projection.MovieShowProjection;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;

public interface IMovieRepository extends JpaRepository<Movie, UUID> {
    Page<Movie> findAll(Pageable pageable);

    Page<Movie> findByActiveTrue(Pageable pageable);

    Page<Movie> findByActiveFalse(Pageable pageable);

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndReleaseDate(String title, LocalDate releaseDate);

    Optional<Movie> findByTitleIgnoreCase(String title);

    Page<Movie> findByActiveTrueAndReleaseDateLessThanEqual(LocalDate date, Pageable pageable);

    Page<Movie> findByActiveTrueAndReleaseDateGreaterThan(LocalDate date, Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Movie> findByGenresContaining(Genre genre, Pageable pageable);

    Page<Movie> findByLanguagesContaining(Language language, Pageable pageable);

    /*
     * =====================================================================
     * ------- Internal Service-to-Service usable repository methods -------
     * =====================================================================
     */
    // renamed to avoid conflict with built-in findById
    Optional<MovieShowProjection> findByMovieId(UUID movieId);
}
