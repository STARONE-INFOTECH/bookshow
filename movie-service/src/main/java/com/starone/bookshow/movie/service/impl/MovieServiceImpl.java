package com.starone.bookshow.movie.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.movie.client.PersonClient;
import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.bookshow.movie.mapper.IMovieMapper;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.IMovieCreditService;
import com.starone.bookshow.movie.service.IMovieService;
import com.starone.common.dto.ApiResponse;
import com.starone.common.dto.MovieResponseDto;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Status;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service("movieService")
@RequiredArgsConstructor
@Transactional
public class MovieServiceImpl implements IMovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieServiceImpl.class);
    private final IMovieRepository movieRepository;
    private final IMovieMapper movieMapper;
    private final PersonClient personClient;
    private final IMovieCreditService movieCreditService; // for enrichment

    @Override
    public MovieResponseDto create(MovieRequestDto requestDto) {
        Movie movie = movieMapper.toEntity(requestDto);
        List<MovieCredit> credits = movie.getMovieCredits();
        Set<UUID> personIds = (credits == null || credits.isEmpty())
                ? Collections.emptySet()
                : credits.stream()
                        .map(MovieCredit::getPersonId)
                        .collect(Collectors.toSet());

        if (!personIds.isEmpty()) {
            ApiResponse<Set<UUID>> validPersonResponse = personClient.validatePersonIds(personIds);
            if (!validPersonResponse.getStatus().equals(Status.SUCCESS)) {
                throw new BadRequestException(ErrorCodes.MOVIE_INVALID_PERSON_IDS,
                        "Person validation failed: " + validPersonResponse.getMessage());
            }

            // Batch call to Person service
            Set<UUID> validIds = validPersonResponse.getData();

            // Find Invalid Ids
            Set<UUID> invalidIds = personIds.stream()
                    .filter(id -> !validIds.contains(id))
                    .collect(Collectors.toSet());

            if (!invalidIds.isEmpty()) {
                throw new BadRequestException(ErrorCodes.MOVIE_INVALID_PERSON_IDS,
                        "The following person IDs do not exist: " + invalidIds +
                                ". Please create the persons first or correct the IDs.");
            }
        }
        // Sync the bidirectional relationship:
        if (movie.getMovieCredits() != null && !movie.getMovieCredits().isEmpty()) {
            movie.getMovieCredits().forEach(credit -> credit.setMovie(movie));
        }

        Movie savedMovie = movieRepository.save(movie);
        return enrichMovieResponse(savedMovie);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponseDto getById(UUID id) {
        Objects.requireNonNull(id, "Movie Id is required");
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Movie not found with id : {}", id);
                    return new NotFoundException(
                            ErrorCodes.MOVIE_NOT_FOUND,
                            "Movie not found with id: " + id);
                });

        log.info("Movie fetched successfully. id={}", id);
        return movieMapper.toResponseDto(movie);
    }

    @Override
    public MovieResponseDto update(UUID id, MovieRequestDto movieRequestDto) {

        Objects.requireNonNull(id, "Movie Id is required.");
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCodes.MOVIE_NOT_FOUND,
                        "Movie not found with id: " + id));
        // mapping MovieDto with existingMovie
        movieMapper.updateEntity(movieRequestDto, existingMovie);

        Movie updatedMovie = movieRepository.save(existingMovie);
        log.info("Movie updated successfully. id={}", id);

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
                .findByTitleContainingIgnoreCase(name, pageable)
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
        return movieRepository.existsByTitleIgnoreCase(name);
    }

    @Override
    public void deleteById(UUID id) {
        if (!movieRepository.existsById(id)) {
            throw new NotFoundException(
                    ErrorCodes.MOVIE_NOT_FOUND,
                    "Movie not found with id: " + id);
        }

        movieRepository.deleteById(id);
        log.info("Movie deleted successfully. id={}", id);
    }

    // Helper to enrich with credits (reuse from getById logic)
    private MovieResponseDto enrichMovieResponse(Movie movie) {
        MovieResponseDto dto = movieMapper.toResponseDto(movie);
        log.info("setting movie credits");
        dto.setMovieCredits(movieCreditService.getCreditsByMovieId(movie.getId()));
        return dto;
    }
}
