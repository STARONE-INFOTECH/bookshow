package com.starone.bookshow.movie.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
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
import com.starone.bookshow.movie.service.IMovieService;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Profession;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.NotFoundException;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.MovieCreditResponse;
import com.starone.common.response.record.MovieResponse;
import com.starone.common.response.record.PersonProfessionAddition;

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

    @Override
    public MovieResponse create(MovieRequestDto requestDto) {

        validateMovieRequest(requestDto);

        Movie movie = mapToMovie(requestDto);

        List<MovieCreditRequestDto> credits = Optional.ofNullable(requestDto.getMovieCredits())
                .orElse(Collections.emptyList());

        Movie savedMovie = movieRepository.save(movie);
        log.debug("Movie saved: {}", savedMovie.getId());

        List<MovieCreditPersonResponse> persons = credits.isEmpty()
                ? List.of()
                : getPersonResponseWithNewProfessions(credits);

        syncProfessions(persons);

        List<MovieCreditResponse> creditResponses = buildCreditResponses(persons,
                savedMovie);

        return buildMovieResponse(savedMovie, creditResponses);
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

        List<MovieCredit> credits = Optional.ofNullable(movie.getMovieCredits())
                .orElse(Collections.emptyList());

        Set<UUID> personIds = credits.stream().map(MovieCredit::getPersonId)
                .collect(Collectors.toSet());

        List<MovieCreditPersonResponse> persons = personClient.getAllPersonByIds(personIds);

        List<MovieCreditResponse> creditResponses = buildCreditResponses(persons, movie);

        log.info("Movie fetched successfully. id={}", id);
        return buildMovieResponse(movie, creditResponses);
    }

    @Override
    public MovieResponse update(UUID id, MovieRequestDto movieRequestDto) {
        if (id == null) {
            throw new BadRequestException(
                    ErrorCodes.BAD_REQUEST,
                    "Movie Id can not be null");
        }

        validateMovieRequest(movieRequestDto);

        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCodes.MOVIE_NOT_FOUND,
                        "Movie not found with id: " + id));

        // mapping MovieDto with existingMovie
        movieMapper.updateEntity(movieRequestDto, existingMovie);

        Movie updatedMovie = movieRepository.save(existingMovie);
        log.info("Movie updated successfully. id={}", id);

        List<MovieCreditRequestDto> credits = Optional.ofNullable(movieRequestDto.getMovieCredits())
                .orElse(Collections.emptyList());

        List<MovieCreditPersonResponse> persons = movieRequestDto.getMovieCredits().isEmpty()
                ? List.of()
                : getPersonResponseWithNewProfessions(credits);

        syncProfessions(persons);

        List<MovieCreditResponse> creditResponses = buildCreditResponses(persons, updatedMovie);

        return buildMovieResponse(updatedMovie, creditResponses);
    }

    @Override
    public MovieResponse deactivate(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.MOVIE_NOT_FOUND));
        if(Boolean.FALSE.equals(movie.getActive())){
            return movieMapper.toResponseDto(movie);
        }
        movie.setActive(false);
        movie = movieRepository.save(movie);
        return movieMapper.toResponseDto(movie);
    }

    @Override
    public MovieResponse activate(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.MOVIE_NOT_FOUND));
        if (Boolean.TRUE.equals(movie.getActive())) {
            return movieMapper.toResponseDto(movie);
        }
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
     * ------ Helper to enrich with credits (reuse from getById logic)------
     * =====================================================================
     */
    private void validateMovieRequest(MovieRequestDto requestDto) {
        if (requestDto == null) {
            throw new BadRequestException(
                    ErrorCodes.BAD_REQUEST,
                    "Movie requestDto is null");
        }
        List<MovieCreditRequestDto> credits = Optional.ofNullable(requestDto.getMovieCredits())
                .orElse(Collections.emptyList());

        if (credits.isEmpty()) {
            return;
        }

        Set<UUID> personIds = new HashSet<>();
        for (MovieCreditRequestDto credit : credits) {
            UUID personId = credit.getPersonId();
            if (personId == null) {
                throw new BadRequestException(
                        ErrorCodes.BAD_REQUEST,
                        "PersonId cannot be null in movie credits");
            }
            if (!personIds.add(personId)) {
                throw new BadRequestException(
                        ErrorCodes.BAD_REQUEST,
                        "Same person cannot be added multiple times as movie credit");
            }
        }

    }

    // Map movie and normalize credits and empty list if null (allow empty credits)
    private Movie mapToMovie(MovieRequestDto requestDto) {
        List<MovieCreditRequestDto> credits = Optional.ofNullable(requestDto.getMovieCredits())
                .orElse(Collections.emptyList());

        Movie movie = movieMapper.toEntity(requestDto);

        credits.forEach(creditRequest -> {
            MovieCredit credit = creditMapper.toEntity(creditRequest);
            movie.addMovieCredit(credit); // owning side set here
        });
        return movie;
    }

    private void syncProfessions(List<MovieCreditPersonResponse> persons) {
        List<PersonProfessionAddition> newProfessions = persons.stream()
                .filter(p -> !p.professions().isEmpty())
                .map(person -> new PersonProfessionAddition(
                        person.id(),
                        person.professions()))
                .toList();
        if (newProfessions.isEmpty()) {
            return;
        }

        try {
            personClient.addProfessionsToPersons(newProfessions);
            log.info("Synced {} professions for movie",
                    newProfessions.size());
        } catch (Exception e) {
            log.error("Failed to sync professions for movie :", e);
            throw new BadRequestException(
                    ErrorCodes.BAD_REQUEST,
                    "Failed to sync professions for movie");
        }
    }

    private List<MovieCreditResponse> buildCreditResponses(
            List<MovieCreditPersonResponse> persons,
            Movie savedMovie) {

        List<MovieCredit> credits = savedMovie.getMovieCredits() == null
                ? new ArrayList<>()
                : savedMovie.getMovieCredits();

        Map<UUID, MovieCreditPersonResponse> personsById = persons.stream()
                .collect(Collectors.toMap(
                        MovieCreditPersonResponse::id,
                        Function.identity()));

        Map<UUID, MovieCredit> creditByPersonId = savedMovie.getMovieCredits().stream()
                .collect(Collectors.toMap(
                        MovieCredit::getPersonId,
                        Function.identity(),
                        (a, b) -> a // safeguard for duplicates
                ));

        return credits.stream()
                .map(creditReq -> {

                    UUID personId = creditReq.getPersonId();

                    MovieCreditPersonResponse person = personsById.get(personId);
                    if (person == null) {
                        throw new NotFoundException(
                                ErrorCodes.PERSON_NOT_FOUND,
                                "Person not found: " + personId);
                    }

                    MovieCredit savedCredit = creditByPersonId.get(personId);
                    if (savedCredit == null) {
                        throw new IllegalStateException(
                                "MovieCredit not found for personId: " + personId);
                    }

                    return new MovieCreditResponse(
                            savedCredit.getId(),
                            personId,
                            person.name(),
                            person.profileImg(),
                            creditReq.getProfessions(),
                            creditReq.getMovieCharacters(),
                            savedCredit.getBillingOrder());
                })
                .toList();
    }

    private MovieResponse buildMovieResponse(Movie savedMovie,
            List<MovieCreditResponse> creditResponses) {

        MovieResponse base = movieMapper.toResponseDto(savedMovie);

        return new MovieResponse(
                base.id(),
                base.title(),
                base.originalTitle(),
                base.synopsis(),
                base.languages(),
                base.genres(),
                creditResponses,
                base.durationMinutes(),
                base.rating(),
                base.releaseDate(),
                base.posterUrl(),
                base.trailerUrl(),
                base.active());
    }

    private List<MovieCreditPersonResponse> getPersonResponseWithNewProfessions(
            List<MovieCreditRequestDto> credits) {
        // Extract person IDs
        Set<UUID> personIds = credits.stream()
                .map(MovieCreditRequestDto::getPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // existing persons from person service
        List<MovieCreditPersonResponse> persons = personClient.getAllPersonByIds(personIds);

        Set<UUID> foundIds = persons.stream()
                .map(MovieCreditPersonResponse::id)
                .collect(Collectors.toSet());

        Set<UUID> missingIds = new HashSet<>(personIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new NotFoundException(
                    ErrorCodes.PERSON_NOT_FOUND,
                    "Missing persons ");
        }

        // map of requested professions
        Map<UUID, Set<Profession>> requestedPersonProfessionsMap = credits.stream()
                .collect(Collectors.toMap(
                        MovieCreditRequestDto::getPersonId,
                        dto -> new HashSet<>(dto.getProfessions()),
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        }));

        return persons.stream()
                .map(person -> {
                    UUID personId = person.id();
                    Set<Profession> requested = requestedPersonProfessionsMap.getOrDefault(personId,
                            Collections.emptySet());

                    // Professions that are requested but not already present
                    Set<Profession> newProfessions = requested.stream()
                            .filter(profession -> !person.professions()
                                    .contains(profession))
                            .collect(Collectors.toUnmodifiableSet());

                    // set new professions to movie credit response to save in person service
                    return new MovieCreditPersonResponse(
                            personId,
                            person.name(),
                            person.profileImg(),
                            newProfessions);
                }).toList();

    }

}
