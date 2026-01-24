package com.starone.bookshow.movie.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.common.response.record.MovieResponse;
import com.starone.common.response.record.MovieShowResponse;

public interface IMovieService {

    MovieResponse create(MovieRequestDto movieRequestDto);

    MovieResponse getById(UUID id);

    MovieResponse update(UUID id, MovieRequestDto movieRequestDto);

    MovieResponse deactivate(UUID id);

    MovieResponse activate(UUID id);

    Page<MovieResponse> getNowShowing(Pageable pageable);

    Page<MovieResponse> getUpcoming(Pageable pageable);

    Page<MovieResponse> getAll(Pageable pageable);

    Page<MovieResponse> searchByTitle(String title, Pageable pageable);

    Page<MovieResponse> filterByGenre(String genre, Pageable pageable);

    Page<MovieResponse> filterByLanguage(String language, Pageable pageable);

    Page<MovieResponse> searchByName(String name, Pageable pageable);

    Page<MovieResponse> getAllActive(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    void deleteById(UUID id);

    /*
     * ====================================================================
     * --- Internal Service-To-Service usable methods with Feign client ---
     * ====================================================================
     */
    MovieShowResponse getByMovieId(UUID movieId);
}
