package com.starone.bookshow.movie.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.dto.MovieResponseDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.mapper.IMovieMapper;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.IMovieCreditService;
import com.starone.bookshow.movie.service.IMovieService;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service("movieService")
@RequiredArgsConstructor
@Transactional
public class MovieServiceImpl implements IMovieService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovieServiceImpl.class);
    private final IMovieRepository movieRepository;
    private final IMovieMapper movieMapper;
    private final IMovieCreditService movieCreditService; // for enrichment

    @Override
    public MovieResponseDto create(MovieRequestDto requestDto) {
        // Optional: check for duplicate title + releaseDate
        if (movieRepository.existsByTitleIgnoreCaseAndReleaseDate(requestDto.getTitle(), requestDto.getReleaseDate())) {
            throw new ConflictException(ErrorCodes.MOVIE_ALREADY_EXISTS,
                    "Movie with same title and release date already exists");
        }

        Movie movie = movieMapper.toEntity(requestDto);
        movie = movieRepository.save(movie);
        return enrichMovieResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponseDto getById(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCodes.NOT_FOUND,
                        "Movie not found with id: " + id));

        LOGGER.debug("Movie fetched successfully. id={}", id);
        return movieMapper.toResponseDto(movie);
    }

    @Override
    public MovieResponseDto update(UUID id, MovieRequestDto movieRequestDto) {

        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCodes.NOT_FOUND,
                        "Movie not found with id: " + id));

        movieMapper.updateEntity(movieRequestDto, existingMovie);

        Movie updatedMovie = movieRepository.save(existingMovie);
        LOGGER.info("Movie updated successfully. id={}", id);

        return movieMapper.toResponseDto(updatedMovie);
    }

    @Override
    public MovieResponseDto deactivate(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.MOVIE_NOT_FOUND));
        movie.setActive(false);
        movie = movieRepository.save(movie);
        return enrichMovieResponse(movie);
    }

    @Override
    public MovieResponseDto activate(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.MOVIE_NOT_FOUND));
        movie.setActive(true);
        movie = movieRepository.save(movie);
        return enrichMovieResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> getNowShowing(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<Movie> page = movieRepository.findByActiveTrueAndReleaseDateLessThanEqual(today, pageable);
        return page.map(this::enrichMovieResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> getUpcoming(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<Movie> page = movieRepository.findByActiveTrueAndReleaseDateGreaterThan(today, pageable);
        return page.map(this::enrichMovieResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> getAll(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> searchByTitle(String title, Pageable pageable) {
        Page<Movie> page = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        return page.map(this::enrichMovieResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> filterByGenre(String genre, Pageable pageable) {
        Genre genreEnum = Genre.valueOf(genre.toUpperCase()); // or custom parsing
        Page<Movie> page = movieRepository.findByGenresContaining(genreEnum, pageable);
        return page.map(this::enrichMovieResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> filterByLanguage(String language, Pageable pageable) {
        Language langEnum = Language.valueOf(language.toUpperCase());
        Page<Movie> page = movieRepository.findByLanguagesContaining(langEnum, pageable);
        return page.map(this::enrichMovieResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> searchByName(String name, Pageable pageable) {
        return movieRepository
                .findByNameContainingIgnoreCase(name, pageable)
                .map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> getAllActive(Pageable pageable) {
        return movieRepository.findByActiveTrue(pageable)
                .map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameIgnoreCase(String name) {
        return movieRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public void deleteById(UUID id) {
        if (!movieRepository.existsById(id)) {
            throw new NotFoundException(
                    ErrorCodes.NOT_FOUND,
                    "Movie not found with id: " + id);
        }

        movieRepository.deleteById(id);
        LOGGER.info("Movie deleted successfully. id={}", id);
    }

    // Helper to enrich with credits (reuse from getById logic)
    private MovieResponseDto enrichMovieResponse(Movie movie) {
        MovieResponseDto dto = movieMapper.toResponseDto(movie);
        dto.setMovieCredits(movieCreditService.getCreditsByMovieId(movie.getId()));
        return dto;
    }
}
