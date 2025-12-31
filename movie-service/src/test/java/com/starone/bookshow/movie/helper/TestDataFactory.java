package com.starone.bookshow.movie.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.common.dto.MovieCreditResponseDto;
import com.starone.common.dto.MovieResponseDto;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Profession;
import com.starone.common.enums.Rating;

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

    // Invalid person Id
    public static MovieRequestDto createMovieRequestWithInvalidPersonId() {
        MovieRequestDto requestDto = createValidMovieRequestDto();
        requestDto.getMovieCredits().get(0).setPersonId(INVALID_PERSON_ID_1);
        requestDto.getMovieCredits().get(1).setPersonId(INVALID_PERSON_ID_2);
        return requestDto;
    }

    // ================== Movie Mapping ==================
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

        List<MovieCredit> credits = movieRequestDto.getMovieCredits().stream()
                .map(creditDto -> {
                    MovieCredit credit = new MovieCredit();
                    credit.setPersonId(creditDto.getPersonId());
                    credit.setProfessions(creditDto.getProfessions());
                    credit.setMovieCharacters(creditDto.getCharacterNames());
                    credit.setBillingOrder(creditDto.getBillingOrder());
                    return credit;
                }).toList();
        movie.setMovieCredits(credits);
        return movie;
    }

    // response from Entity
    public static MovieResponseDto createResponseFromMovie(Movie movie) {
        MovieResponseDto movieResponseDto = new MovieResponseDto();
        movieResponseDto.setId(movie.getId());
        movieResponseDto.setTitle(movie.getTitle());
        movieResponseDto.setOriginalTitle(movie.getOriginalTitle());
        movieResponseDto.setSynopsis(movie.getSynopsis());
        movieResponseDto.setLanguages(movie.getLanguages());
        movieResponseDto.setGenres(movie.getGenres());
        movieResponseDto.setDurationMinutes(movie.getDurationMinutes());
        movieResponseDto.setRating(movie.getRating());
        movieResponseDto.setReleaseDate(movie.getReleaseDate());
        movieResponseDto.setPosterUrl(movie.getPosterUrl());
        movieResponseDto.setTrailerUrl(movie.getTrailerUrl());

        List<MovieCreditResponseDto> credits = movie.getMovieCredits().stream()
                .map(credit -> {
                    MovieCreditResponseDto creditResponse = new MovieCreditResponseDto();
                    creditResponse.setPersonId(credit.getPersonId());
                    creditResponse.setProfessions(credit.getProfessions());
                    creditResponse.setCharacterNames(credit.getMovieCharacters());
                    creditResponse.setBillingOrder(credit.getBillingOrder());
                    return creditResponse;
                }).toList();
        movieResponseDto.setMovieCredits(credits);
        return movieResponseDto;
    }
}
