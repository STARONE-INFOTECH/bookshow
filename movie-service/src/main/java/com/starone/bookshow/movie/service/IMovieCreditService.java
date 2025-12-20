package com.starone.bookshow.movie.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.common.dto.MovieCreditResponseDto;
import com.starone.common.enums.Profession;

public interface IMovieCreditService {

  /**
     * Add a new cast/crew credit to a movie
     */
    MovieCreditResponseDto addCredit(UUID movieId, MovieCreditRequestDto requestDto);

    /**
     * Update an existing credit
     */
    MovieCreditResponseDto updateCredit(UUID creditId, MovieCreditRequestDto requestDto);

    /**
     * Remove a credit from a movie
     */
    void removeCredit(UUID creditId);

    /**
     * Get all enriched credits for a movie (non-paginated - used in movie details)
     */
    List<MovieCreditResponseDto> getCreditsByMovieId(UUID movieId);

    /**
     * Get paginated enriched credits for a movie (admin view)
     */
    Page<MovieCreditResponseDto> getCreditsByMovieIdPaginated(UUID movieId, Pageable pageable);

    /**
     * Get single enriched credit by ID
     */
    MovieCreditResponseDto getCreditById(UUID creditId);

    /**
     * Reorder credits by billing order (admin drag-and-drop)
     */
    List<MovieCreditResponseDto> reorderCredits(UUID movieId, List<MovieCreditRequestDto> orderedCredits);

    /**
     * Check if credit already exists (prevent duplicates)
     */
    boolean existsCredit(UUID movieId, UUID personId, Profession role);
}
