package com.starone.bookshow.movie.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.movie.client.IPersonClient;
import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.bookshow.movie.mapper.IMovieCreditMapper;
import com.starone.bookshow.movie.mapper.IMovieMapper;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.IMovieCreditService;
import com.starone.bookshow.movie.service.IMovieService;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Profession;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.NotFoundException;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.MovieResponse;
import com.starone.common.response.record.PersonProfessionSync;

import lombok.RequiredArgsConstructor;

@Service("movieService")
@RequiredArgsConstructor
@Transactional
public class MovieServiceImpl implements IMovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieServiceImpl.class);
    private final IMovieRepository movieRepository;
    private final IMovieMapper movieMapper;
    private final IMovieCreditMapper creditMapper;
    private final IPersonClient personClient;
    private final IMovieCreditService movieCreditService; // for enrichment

    @Override
    public MovieResponse create(MovieRequestDto requestDto) {
        if (requestDto == null) {
            throw new BadRequestException(
                    ErrorCodes.BAD_REQUEST,
                    "Movie requestDto is null");
        }
        // Normalize credits to empty list if null (allow empty credits)
        List<MovieCreditRequestDto> credits = Optional.ofNullable(requestDto.getMovieCredits())
                .orElse(Collections.emptyList());

        // Extract person IDs (safe even if credits is empty)
        Set<UUID> personIds = credits.stream()
                .map(MovieCreditRequestDto::getPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Movie movie = movieMapper.toEntity(requestDto);

        // Add credits if any
        credits.forEach(creditRequest -> {
            MovieCredit credit = creditMapper.toEntity(creditRequest);
            movie.addMovieCredit(credit);
        });

        Movie savedMovie = movieRepository.save(movie);
        log.debug("Movie saved: {} [corr={}]", savedMovie.getId(), null);
        
        List<PersonProfessionSync> syncProfessions = syncProfessions(personIds, credits);
        if (!syncProfessions.isEmpty()) {
            try {
                personClient.addProfessionsBulk(syncProfessions);
                log.info("Synced {} professions [corr={}]", syncProfessions.size(), null);
            } catch (Exception e) {
                log.error("Failed to sync professions for movie {} [corr={}]", savedMovie.getId(), null, e);
                throw new BadRequestException(ErrorCodes.BAD_REQUEST, "Failed to sync professions for movie");
            }

        }

        return movieMapper.toResponseDto(savedMovie);
       
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getById(UUID id) {
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
    public MovieResponse update(UUID id, MovieRequestDto movieRequestDto) {

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
    public MovieResponse deactivate(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.MOVIE_NOT_FOUND));
        movie.setActive(false);
        movie = movieRepository.save(movie);
        return movieMapper.toResponseDto(movie);
    }

    @Override
    public MovieResponse activate(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.MOVIE_NOT_FOUND));
        movie.setActive(true);
        movie = movieRepository.save(movie);
        return movieMapper.toResponseDto(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getNowShowing(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<Movie> page = movieRepository.findByActiveTrueAndReleaseDateLessThanEqual(today, pageable);
        return page.map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getUpcoming(Pageable pageable) {
        LocalDate today = LocalDate.now();
        Page<Movie> page = movieRepository.findByActiveTrueAndReleaseDateGreaterThan(today, pageable);
        return page.map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getAll(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchByTitle(String title, Pageable pageable) {
        Page<Movie> page = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        return page.map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> filterByGenre(String genre, Pageable pageable) {
        Genre genreEnum = Genre.valueOf(genre.toUpperCase()); // or custom parsing
        Page<Movie> page = movieRepository.findByGenresContaining(genreEnum, pageable);
        return page.map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> filterByLanguage(String language, Pageable pageable) {
        Language langEnum = Language.valueOf(language.toUpperCase());
        Page<Movie> page = movieRepository.findByLanguagesContaining(langEnum, pageable);
        return page.map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchByName(String name, Pageable pageable) {
        return movieRepository
                .findByTitleContainingIgnoreCase(name, pageable)
                .map(movieMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getAllActive(Pageable pageable) {
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

    /*
     * =====================================================================
     * --- Helper to enrich with credits (reuse from getById logic)----
     * =====================================================================
     */
    private List<PersonProfessionSync> syncProfessions(Set<UUID> personIds, List<MovieCreditRequestDto> credits) {
        // existing persons from person service
        List<MovieCreditPersonResponse> persons = personClient.getAllPersonByIds(personIds);
        if (persons.size() != personIds.size()) {
            throw new NotFoundException(ErrorCodes.PERSON_NOT_FOUND, "Missing persons ");
        }

        // map of existing professions
        Map<UUID, Set<Profession>> existingProfessionMap = persons.stream()
                .collect(Collectors.toMap(
                        MovieCreditPersonResponse::id,
                        p -> Set.copyOf(p.professions())));

        // map of requested professions
        Map<UUID, Set<Profession>> requestedProfessionMap = credits.stream()
                .collect(Collectors.toMap(
                        MovieCreditRequestDto::getPersonId,
                        MovieCreditRequestDto::getProfessions));

        return requestedProfessionMap.entrySet().stream()
                .map(entry -> {
                    UUID personId = entry.getKey();
                    Set<Profession> requested = entry.getValue(); // new profession

                    Set<Profession> existing = existingProfessionMap
                            .getOrDefault(personId, Set.of());

                    // Professions that are requested but not already present
                    Set<Profession> newOnes = requested.stream()
                            .filter(p -> !existing.contains(p))
                            .collect(Collectors.toSet());

                    return newOnes.isEmpty()
                            ? null
                            : new PersonProfessionSync(personId, newOnes);
                })
                .filter(Objects::nonNull)
                .toList();
    }

}
