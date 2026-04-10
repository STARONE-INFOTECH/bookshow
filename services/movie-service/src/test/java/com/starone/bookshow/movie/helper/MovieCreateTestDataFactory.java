package com.starone.bookshow.movie.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.entity.MovieCredit;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Profession;
import com.starone.common.enums.Rating;
import com.starone.springcommon.response.record.MovieCreditPersonResponse;
import com.starone.springcommon.response.record.MovieResponse;

public class MovieCreateTestDataFactory {
    private MovieCreateTestDataFactory() {
    }

    /*
     * =====================================================
     * ===================== CONSTANTS =====================
     * =====================================================
     */
    public static final UUID MOVIE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static final UUID CREDIT_ID_1 = UUID.fromString("22222222-2222-2222-2222-222222222222");

    public static final UUID CREDIT_ID_2 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    public static final UUID PERSON_ID_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    public static final UUID PERSON_ID_2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    public static final Set<String> MAIN_CHARACTER = Set.of("John Wick");
    public static final Set<Profession> ACTOR_ONLY = Set.of(Profession.ACTOR);

    public static final Set<Profession> ACTOR_DIRECTOR = Set.of(Profession.ACTOR, Profession.DIRECTOR);

    public static final Set<Profession> ACTOR_PRODUCER = Set.of(Profession.ACTOR, Profession.PRODUCER);

    /*
     * =====================================================
     * REQUEST STATE (MOVIE CREATE REQUEST DTO's)
     * =====================================================
     */
    private static MovieRequestDto baseCreateRequest(List<MovieCreditRequestDto> credits) {
        return new MovieRequestDto(
                "Inception",
                "Inception",
                "Dream within a dream",
                List.of(Language.ENGLISH),
                List.of(Genre.SCI_FI),
                credits,
                148,
                Rating.PG_13,
                LocalDate.of(2010, 7, 16),
                "poster.jpg",
                "trailer.mp4");
    }

    public static MovieRequestDto movieWithNullCredits() {
        return baseCreateRequest(null);
    }

    public static MovieRequestDto movieWithEmptyCredits() {
        return baseCreateRequest(Collections.emptyList());
    }

    public static MovieRequestDto movieWithOneCredit() {
        return baseCreateRequest(List.of(singleCredit()));
    }

    public static MovieRequestDto movieWithTwoDiffPersonCredits() {
        return baseCreateRequest(twoDifferentPersonCredits());
    }

    public static MovieRequestDto movieWithDuplicatePersonCredits() {
        return baseCreateRequest(List.of(
                credit(PERSON_ID_1, ACTOR_ONLY, 1),
                credit(PERSON_ID_1, ACTOR_PRODUCER, 2)));
    }

    public static MovieRequestDto movieWithNullPersonCredits() {
        return baseCreateRequest(List.of(
                new MovieCreditRequestDto(
                        null,
                        ACTOR_DIRECTOR,
                        MAIN_CHARACTER,
                        1)));
    }

    /*
     * =====================================================
     * CREDIT REQUEST HELPER's)
     * =====================================================
     */
    private static MovieCreditRequestDto credit(
            UUID personId,
            Set<Profession> professions,
            int billingOrder) {
        return new MovieCreditRequestDto(
                personId,
                professions,
                MAIN_CHARACTER,
                billingOrder);
    }

    private static MovieCreditRequestDto singleCredit() {
        return credit(PERSON_ID_1, ACTOR_DIRECTOR, 1);
    }

    private static List<MovieCreditRequestDto> twoDifferentPersonCredits() {
        return List.of(
                credit(PERSON_ID_1, ACTOR_DIRECTOR, 1),
                credit(PERSON_ID_2, ACTOR_PRODUCER, 2));
    }

    /*
     * =====================================================
     * SAVED MOVIE ENTITIES (POST CREATE)
     * =====================================================
     */
    public static Movie savedMovieWithOneCredit() {
        Movie movie = new Movie();
        movie.setId(MOVIE_ID);

        MovieCredit credit = new MovieCredit();
        credit.setId(CREDIT_ID_1);
        credit.setPersonId(PERSON_ID_1);
        credit.setBillingOrder(1);
        credit.setMovie(movie);

        movie.setMovieCredits(new ArrayList<>(List.of(credit)));
        return movie;
    }

    public static Movie savedMovieWithTwoCreditsDifferentPerson() {
        Movie movie = new Movie();
        movie.setId(MOVIE_ID);

        MovieCredit c1 = new MovieCredit();
        c1.setId(CREDIT_ID_1);
        c1.setPersonId(PERSON_ID_1);
        c1.setBillingOrder(1);
        c1.setMovie(movie);

        MovieCredit c2 = new MovieCredit();
        c2.setId(CREDIT_ID_2);
        c2.setPersonId(PERSON_ID_2);
        c2.setBillingOrder(2);
        c2.setMovie(movie);

        movie.setMovieCredits(new ArrayList<>(List.of(c1, c2)));
        return movie;
    }

    /*
     * =====================================================
     * PERSON SERVICE RESPONSES
     * =====================================================
     */
    // Person already has all requested professions
    public static MovieCreditPersonResponse personWithAllRequestedProfessions() {
        return new MovieCreditPersonResponse(
                PERSON_ID_1,
                "Leonardo DiCaprio",
                "leo.jpg",
                ACTOR_DIRECTOR);
    }

    // Missing DIRECTOR
    public static MovieCreditPersonResponse personMissingRequestedProfessions_1() {
        return new MovieCreditPersonResponse(
                PERSON_ID_1,
                "Leonardo DiCaprio",
                "leo.jpg",
                ACTOR_ONLY);
    }

    // Missing ACTOR + PRODUCER
    public static MovieCreditPersonResponse personMissingRequestedProfessions_2() {
        return new MovieCreditPersonResponse(
                PERSON_ID_2,
                "Christopher Nolan",
                "nolan.jpg",
                Set.of(Profession.DIRECTOR));
    }

    // No new professions needed
    public static MovieCreditPersonResponse personWithRequestedProfessions_1() {
        return new MovieCreditPersonResponse(
                PERSON_ID_1,
                "Leonardo DiCaprio",
                "leo.jpg",
                ACTOR_DIRECTOR);
    }

    public static MovieCreditPersonResponse personWithRequestedProfessions_2() {
        return new MovieCreditPersonResponse(
                PERSON_ID_2,
                "Christopher Nolan",
                "nolan.jpg",
                ACTOR_PRODUCER);
    }

    /*
     * =====================================================
     * BASE RESPONSE
     * =====================================================
     */
    public static MovieResponse baseMovieResponse(boolean isActive) {
        return new MovieResponse(
                MOVIE_ID,
                "Inception",
                "Inception",
                "Dream within a dream",
                List.of(Language.ENGLISH),
                List.of(Genre.SCI_FI),
                new ArrayList<>(),
                148,
                Rating.PG_13,
                LocalDate.of(2010, 7, 16),
                "poster.jpg",
                "trailer.mp4",
                isActive);
    }

}
