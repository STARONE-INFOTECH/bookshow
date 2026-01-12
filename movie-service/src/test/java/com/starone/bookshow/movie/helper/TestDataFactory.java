package com.starone.bookshow.movie.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Profession;
import com.starone.common.enums.Rating;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.MovieCreditResponse;
import com.starone.common.response.record.MovieResponse;

public class TestDataFactory {

    // Pre-defined "known invalid" UUIDs
    private static final UUID INVALID_PERSON_ID_1 = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID INVALID_PERSON_ID_2 = UUID.fromString("88888888-8888-8888-8888-888888888888");

    // ==================== MovieRequestDto ====================
    public static MovieRequestDto createValidMovieRequestDto() {
        MovieRequestDto movieDto = new MovieRequestDto();
        movieDto.setTitle("Inception");
        movieDto.setOriginalTitle("Inception");
        movieDto.setSynopsis("A thief who steals corporate secrets through dream-sharing technology.");
        movieDto.setLanguages(List.of(Language.ENGLISH, Language.HINDI));
        movieDto.setGenres(List.of(Genre.SCI_FI, Genre.THRILLER));
        movieDto.setDurationMinutes(148);
        movieDto.setRating(Rating.PG_13);
        movieDto.setReleaseDate(LocalDate.of(2010, 7, 16));
        movieDto.setPosterUrl("https://example.com/poster.jpg");
        movieDto.setTrailerUrl("https://example.com/trailer.mp4");
        movieDto.setMovieCredits(List.of(
                new MovieCreditRequestDto(UUID.randomUUID(), Set.of(Profession.ACTOR), Set.of("Dom Cobb"), 1),
                new MovieCreditRequestDto(UUID.randomUUID(), Set.of(Profession.DIRECTOR), null, 0)));
        return movieDto;
    }

    // ================== Movie ==================
    // Entity from Dto
    public static Movie createMovieFromDto(MovieRequestDto movieRequestDto) {
        Movie movie = new Movie();
        movie.setTitle(movieRequestDto.getTitle());
        movie.setOriginalTitle(movieRequestDto.getOriginalTitle());
        movie.setSynopsis(movieRequestDto.getSynopsis());
        movie.setLanguages(movieRequestDto.getLanguages());
        movie.setGenres(movieRequestDto.getGenres());
        movie.setDurationMinutes(movieRequestDto.getDurationMinutes());
        movie.setRating(movieRequestDto.getRating());
        movie.setReleaseDate(movieRequestDto.getReleaseDate());
        movie.setPosterUrl(movieRequestDto.getPosterUrl());
        movie.setTrailerUrl(movieRequestDto.getTrailerUrl());
        List<MovieCredit> creditResponse = Optional.ofNullable(movieRequestDto.getMovieCredits())
                .orElse(List.of())
                .stream()
                .map(credit -> {
                    MovieCredit movieCredit = new MovieCredit();
                    movieCredit.setId(UUID.randomUUID());
                    movieCredit.setPersonId(credit.getPersonId());
                    movieCredit.setMovieCharacters(
                            credit.getMovieCharacters() == null
                                    ? Set.of()
                                    : credit.getMovieCharacters());
                    movieCredit.setProfessions(
                            credit.getProfessions() == null
                                    ? Set.of()
                                    : credit.getProfessions());
                    movieCredit.setBillingOrder(credit.getBillingOrder());
                    return movieCredit;
                }).collect(Collectors.toList());
        movie.setMovieCredits(creditResponse);

        return movie;
    }

    public static Movie createMovieWithEmptyCredits(MovieRequestDto movieRequestDto) {
        Movie movie = new Movie();
        movie.setTitle(movieRequestDto.getTitle());
        movie.setOriginalTitle(movieRequestDto.getOriginalTitle());
        movie.setSynopsis(movieRequestDto.getSynopsis());
        movie.setLanguages(movieRequestDto.getLanguages());
        movie.setGenres(movieRequestDto.getGenres());
        movie.setDurationMinutes(movieRequestDto.getDurationMinutes());
        movie.setRating(movieRequestDto.getRating());
        movie.setReleaseDate(movieRequestDto.getReleaseDate());
        movie.setPosterUrl(movieRequestDto.getPosterUrl());
        movie.setTrailerUrl(movieRequestDto.getTrailerUrl());
        movie.setMovieCredits(new ArrayList<>());

        return movie;
    }

    // saved valid movie
    public static Movie createSavedMovie() {
        MovieRequestDto requestDto = createValidMovieRequestDto();
        Movie movie = createMovieFromDto(requestDto);
        movie.setId(UUID.randomUUID());
        movie.setActive(true);
        return movie;
    }

    // response from Entity
    public static MovieResponse createResponseFromMovie(Movie movie) {
        List<MovieCreditResponse> credits = movie.getMovieCredits().stream()
                .map(credit -> {
                    MovieCreditPersonResponse creditPersonResponse = createMovieCreditResponseFromPerson();
                    return new MovieCreditResponse(
                            UUID.randomUUID(),
                            credit.getPersonId(),
                            creditPersonResponse.name(),
                            creditPersonResponse.profileImg(),
                            credit.getProfessions(),
                            credit.getMovieCharacters(),
                            credit.getBillingOrder());

                }).collect(Collectors.toList());
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getOriginalTitle(),
                movie.getSynopsis(),
                movie.getLanguages(),
                movie.getGenres(),
                credits,
                movie.getDurationMinutes(),
                movie.getRating(),
                movie.getReleaseDate(),
                movie.getPosterUrl(),
                movie.getTrailerUrl(),
                movie.getActive());
    }

    // ================== Movie Credit ==================

    public static MovieCredit createMovieCreditFromDto(MovieCreditRequestDto creditRequestDto) {
        MovieCredit credit = new MovieCredit();
        credit.setPersonId(creditRequestDto.getPersonId());
        credit.setProfessions(creditRequestDto.getProfessions());
        credit.setMovieCharacters(creditRequestDto.getMovieCharacters());
        credit.setBillingOrder(creditRequestDto.getBillingOrder());
        return credit;
    }

    public static MovieCreditPersonResponse createMovieCreditResponseFromPerson() {
        return new MovieCreditPersonResponse(
                UUID.randomUUID(),
                "Leonardo Decrapio",
                "url_image",
                Set.of(Profession.ACTOR));
    }

    public static List<MovieCreditPersonResponse> createPersonsWithMissingProfessions() {
        return List.of(
                new MovieCreditPersonResponse(
                        UUID.randomUUID(),
                        "Leonardo DiCaprio",
                        "https://example.com/leonardo.jpg",
                        Set.of(Profession.ACTOR) // missing DIRECTOR, PRODUCER
                ),
                new MovieCreditPersonResponse(
                        UUID.randomUUID(),
                        "Christopher Nolan",
                        "https://example.com/nolan.jpg",
                        Set.of(Profession.DIRECTOR) // missing ACTOR
                ));
    }

    /**
     * Persons with all required professions (for testing no sync)
     */
    public static List<MovieCreditPersonResponse> createPersonsWithAllProfessions() {
        return List.of(
                new MovieCreditPersonResponse(
                        UUID.randomUUID(),
                        "Leonardo DiCaprio",
                        "https://example.com/leonardo.jpg",
                        Set.of(Profession.ACTOR)),
                new MovieCreditPersonResponse(
                        UUID.randomUUID(),
                        "Christopher Nolan",
                        "https://example.com/nolan.jpg",
                        Set.of(Profession.DIRECTOR)));
    }

    /**
     * Fewer persons than requested IDs (for testing Missing persons exception)
     */
    public static List<MovieCreditPersonResponse> createPersonsWithMissingCount(Set<UUID> requestedIds) {
        List<MovieCreditPersonResponse> persons = new ArrayList<>();
        // Return only first ID — simulate missing persons
        if (!requestedIds.isEmpty()) {
            UUID firstId = requestedIds.iterator().next();
            persons.add(new MovieCreditPersonResponse(
                    firstId,
                    "One Person",
                    "img.jpg",
                    Set.of(Profession.ACTOR)));
        }
        return persons; // size < requestedIds.size() → throws NotFoundException
    }

    /**
     * Empty list (for edge case when no persons returned)
     */
    public static List<MovieCreditPersonResponse> createEmptyPersons() {
        return Collections.emptyList();
    }

}
