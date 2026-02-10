package com.starone.bookshow.movie.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.common.enums.Profession;
import com.starone.springcommon.response.record.MovieCreditResponse;

public interface IMovieCreditService {
  
  /**
   * Get all enriched credits for a movie (non-paginated - used in movie details)
   */
  List<MovieCreditResponse> getCreditsByMovieId(UUID movieId);

  /**
   * Get paginated enriched credits for a movie (admin view)
   */
  Page<MovieCreditResponse> getCreditsByMovieIdPaginated(UUID movieId, Pageable pageable);

  /**
   * Get single enriched credit by ID
   */
  MovieCreditResponse getCreditById(UUID creditId);

  /**
   * Reorder credits by billing order (admin drag-and-drop)
   */
  List<MovieCreditResponse> reorderCredits(UUID movieId, List<MovieCreditRequestDto> orderedCredits);

  /**
   * Check if credit already exists (prevent duplicates)
   */
  boolean existsCredit(UUID movieId, UUID personId, Set<Profession> roles);
}
