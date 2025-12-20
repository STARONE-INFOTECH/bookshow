package com.starone.bookshow.movie.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.common.dto.MovieResponseDto;

public interface IMovieService {

    MovieResponseDto create(MovieRequestDto movieRequestDto);

    MovieResponseDto getById(UUID id);

    MovieResponseDto update(UUID id, MovieRequestDto movieRequestDto);

    MovieResponseDto deactivate(UUID id);

    MovieResponseDto activate(UUID id);

    Page<MovieResponseDto> getNowShowing(Pageable pageable);

    Page<MovieResponseDto> getUpcoming(Pageable pageable);

    Page<MovieResponseDto> getAll(Pageable pageable);

    Page<MovieResponseDto> searchByTitle(String title, Pageable pageable);

    Page<MovieResponseDto> filterByGenre(String genre, Pageable pageable);

    Page<MovieResponseDto> filterByLanguage(String language, Pageable pageable);

    Page<MovieResponseDto> searchByName(String name, Pageable pageable);

    Page<MovieResponseDto> getAllActive(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    void deleteById(UUID id);
}
