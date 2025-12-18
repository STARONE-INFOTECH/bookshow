package com.starone.bookshow.movie.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.starone.bookshow.movie.entity.Movie;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;

public interface IMovieRepository extends JpaRepository<Movie, UUID> {
    Page<Movie> findAll(Pageable pageable);

    Page<Movie> findByActiveTrue(Pageable pageable);

    Page<Movie> findByActiveFalse(Pageable pageable);

    Page<Movie> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByTitleIgnoreCaseAndReleaseDate(String title, LocalDate releaseDate);

    Optional<Movie> findByNameIgnoreCase(String name);

    Page<Movie> findByActiveTrueAndReleaseDateLessThanEqual(LocalDate date, Pageable pageable);

    Page<Movie> findByActiveTrueAndReleaseDateGreaterThan(LocalDate date, Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Movie> findByGenresContaining(Genre genre, Pageable pageable);

    Page<Movie> findByLanguagesContaining(Language language, Pageable pageable);
}
