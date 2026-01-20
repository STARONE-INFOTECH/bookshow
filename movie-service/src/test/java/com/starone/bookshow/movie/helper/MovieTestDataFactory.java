package com.starone.bookshow.movie.helper;

import java.time.LocalDate;
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
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.MovieResponse;

public class MovieTestDataFactory {

    private MovieTestDataFactory() {
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
     * ================== MOVIE REQUEST DTO ================
     * =====================================================
     */
    private static MovieRequestDto baseMovieRequestDto(List<MovieCreditRequestDto> credits) {
        return new MovieRequestDto(
                "Inception",
                "Inception",
                "Dream within a dream",
                List.of(Language.ENGLISH),
                List.of(Genre.SCI_FI),
                credits, // OR null to test normalizeCredits
                148,
                Rating.PG_13,
                LocalDate.of(2010, 7, 16),
                "poster.jpg",
                "trailer.mp4");
    }

    public static MovieRequestDto movieWithEmptyCredits() {
        return baseMovieRequestDto(Collections.emptyList());
    }

    public static MovieRequestDto movieWithOneCredit() {
        return baseMovieRequestDto(List.of(creditWithSinglePerson()));
    }

    public static MovieRequestDto movieWithTwoDiffPersonCredits() {
        return baseMovieRequestDto(creditWithTwoDifferentPersons());
    }

    public static MovieRequestDto movieWithDuplicatePersonCredits() {
        return baseMovieRequestDto(creditWithTwoDuplicatePerson());
    }

    public static MovieRequestDto movieWithNullPersonCredits() {
        return baseMovieRequestDto(creditWithsingleNullPerson());

    }

    public static MovieRequestDto movieWithNullCredits() {
        return baseMovieRequestDto(null);
    }

    /*
     * =====================================================
     * =============== MOVIE CREDIT REQUEST DTO ============
     * =====================================================
     */
    private static MovieCreditRequestDto creditRequest(
            UUID personId,
            Set<Profession> professions,
            Set<String> characters,
            int billingOrder) {

        return new MovieCreditRequestDto(
                personId,
                professions,
                characters,
                billingOrder);
    }

    private static MovieCreditRequestDto creditWithSinglePerson() {
        return creditRequest(PERSON_ID_1, ACTOR_DIRECTOR, MAIN_CHARACTER, 1);
    }

    private static List<MovieCreditRequestDto> creditWithTwoDifferentPersons() {
        return List.of(
                creditRequest(PERSON_ID_1, ACTOR_DIRECTOR, MAIN_CHARACTER, 1),
                creditRequest(PERSON_ID_2, ACTOR_PRODUCER, MAIN_CHARACTER, 2));

    }

    private static List<MovieCreditRequestDto> creditWithsingleNullPerson() {
        return List.of(creditRequest(null, ACTOR_DIRECTOR, MAIN_CHARACTER, 1));
    }

    private static List<MovieCreditRequestDto> creditWithTwoDuplicatePerson() {
        return List.of(
                creditRequest(PERSON_ID_1, ACTOR_ONLY, MAIN_CHARACTER, 1),
                creditRequest(PERSON_ID_1, ACTOR_PRODUCER, MAIN_CHARACTER, 2));

    }

    /*
     * =====================================================
     * =============== MOVIE AND CREDIT ENTITY =============
     * =====================================================
     */
    private static Movie baseSavedMovie() {
        Movie movie = new Movie();
        movie.setId(MOVIE_ID);
        movie.setMovieCredits(Collections.emptyList());
        return movie;
    }

    private static MovieCredit credit(
            UUID creditId,
            UUID personId,
            int billingOrder,
            Movie movie) {
        MovieCredit credit = new MovieCredit();
        credit.setId(creditId);
        credit.setPersonId(personId);
        credit.setBillingOrder(billingOrder);
        credit.setMovie(movie);
        return credit;

    }

    public static Movie savedMovieWithOneCredit() {
        Movie movie = baseSavedMovie();
        MovieCredit credit = credit(
                CREDIT_ID_1, PERSON_ID_1, 1, movie);

        movie.setMovieCredits(List.of(credit));
        return movie;
    }

    public static Movie savedMovieWithTwoCreditsSamePerson() {
        Movie movie = baseSavedMovie();

        MovieCredit credit1 = credit(
                CREDIT_ID_1, PERSON_ID_1, 1, movie);

        MovieCredit credit2 = credit(
                CREDIT_ID_2, PERSON_ID_1, 2, movie);

        movie.setMovieCredits(List.of(credit1, credit2));
        return movie;
    }

    public static Movie savedMovieWithTwoCreditsDifferentPerson() {
        Movie movie = baseSavedMovie();

        MovieCredit credit1 = credit(
                CREDIT_ID_1, PERSON_ID_1, 1, movie);

        MovieCredit credit2 = credit(
                CREDIT_ID_2, PERSON_ID_2, 2, movie);

        movie.setMovieCredits(List.of(credit1, credit2));
        return movie;
    }

    /*
     * =====================================================
     * =========== PERSON SERVICE RESPONSES ================
     * =====================================================
     */
    // MovieRequest professions : [P1 = ACTOR_ONLY] [p2 = ACTOR_PRODUCER]
    public static MovieCreditPersonResponse personWithAllRequestedProfessions() {
        return new MovieCreditPersonResponse(
                PERSON_ID_1,
                "Leonardo DiCaprio",
                "leo.jpg",
                ACTOR_DIRECTOR); // no new profession (Requested) added for this person
    }
    // MovieRequest professions : [P1 = ACTOR_DIRECTOR]
    public static MovieCreditPersonResponse personMissingRequestedProfessions_1() {
        return new MovieCreditPersonResponse(
                PERSON_ID_1,
                "Leonardo DiCaprio",
                "leo.jpg",
                ACTOR_ONLY); // new profession (DIRECTOR)  will be added
    }
    // MovieRequest professions : [p2 = ACTOR_PRODUCER]
    public static MovieCreditPersonResponse personMissingRequestedProfessions_2() {
        return new MovieCreditPersonResponse(
                PERSON_ID_2,
                "Christopher Nolan",
                "nolan.jpg",
                Set.of(Profession.DIRECTOR)); // new profession (ACTOR_PRODUCER) will be added
    }
    // MovieRequest professions : [P1 = ACTOR_DIRECTOR]
    public static MovieCreditPersonResponse personWithRequestedProfessions_1() {
        return new MovieCreditPersonResponse(
                PERSON_ID_1,
                "Leonardo DiCaprio",
                "leo.jpg",
                ACTOR_DIRECTOR); // no new profession 
    }
    // MovieRequest professions : [p2 = ACTOR_PRODUCER]
    public static MovieCreditPersonResponse personWithRequestedProfessions_2() {
        return new MovieCreditPersonResponse(
                PERSON_ID_2,
                "Christopher Nolan",
                "nolan.jpg",
                ACTOR_PRODUCER); //no new profession 
    }

    /*
     * =====================================================
     * =================== BASE RESPONSE ===================
     * =====================================================
     */

    public static MovieResponse baseMovieResponse() {
        return new MovieResponse(
                MOVIE_ID,
                "Inception",
                "Inception",
                "Dream within a dream",
                List.of(Language.ENGLISH),
                List.of(Genre.SCI_FI),
                Collections.emptyList(),
                148,
                Rating.PG_13,
                LocalDate.of(2010, 7, 16),
                "poster.jpg",
                "trailer.mp4",
                true);
    }

}
