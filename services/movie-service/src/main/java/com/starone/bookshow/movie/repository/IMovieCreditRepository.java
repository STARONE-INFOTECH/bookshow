package com.starone.bookshow.movie.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.common.enums.Profession;

public interface IMovieCreditRepository extends JpaRepository<MovieCredit, UUID> {

    List<MovieCredit> findByMovieId(UUID movieId);

    // Paginated version
    Page<MovieCredit> findByMovieId(UUID movieId, Pageable pageable);

    // Find all credits for a specific person (for filmography)
    Page<MovieCredit> findByPersonId(UUID personId, Pageable pageable);

    // Find specific credit for validation/update
    Optional<MovieCredit> findByMovieIdAndPersonId(UUID movieId, UUID personId);

    // Optional: find by credit ID and movie ID for security
    Optional<MovieCredit> findByIdAndMovieId(UUID creditId, UUID movieId);

    // Optional: check credit available by movie ID , personId, and charatcters
    boolean existsByMovieIdAndPersonIdAndMovieCharacters(UUID movieId, UUID personId, Set<Profession> professions);
}
