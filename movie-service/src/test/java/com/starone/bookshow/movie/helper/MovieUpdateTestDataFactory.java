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
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Profession;
import com.starone.common.enums.Rating;
import com.starone.common.response.record.MovieCreditPersonResponse;
import com.starone.common.response.record.MovieResponse;

public class MovieUpdateTestDataFactory {

    private MovieUpdateTestDataFactory() {
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

    /*
     * =====================================================
     * EXISTING STATE (DB BEFORE UPDATE)
     * =====================================================
     */

    public static Movie existingMovie_beforeUpdate() {
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
     * UPDATE REQUEST (CLIENT INTENT)
     * =====================================================
     */

    public static MovieRequestDto updateRequest_withNewProfessions() {
        return new MovieRequestDto(
                "Inception",
                "Inception",
                "Updated synopsis",
                List.of(Language.ENGLISH, Language.HINDI),
                List.of(Genre.SCI_FI),
                List.of(
                        new MovieCreditRequestDto(
                                PERSON_ID_1,
                                Set.of(Profession.ACTOR, Profession.DIRECTOR),
                                MAIN_CHARACTER,
                                1),
                        new MovieCreditRequestDto(
                                PERSON_ID_2,
                                Set.of(Profession.ACTOR, Profession.PRODUCER),
                                MAIN_CHARACTER,
                                2)),
                148,
                Rating.PG_13,
                LocalDate.of(2010, 7, 25),
                "poster.jpg",
                "trailer.mp4");
    }

    /*
     * =====================================================
     * PERSON SERVICE STATE (BEFORE UPDATE)
     * =====================================================
     */

    public static List<MovieCreditPersonResponse> persons_beforeUpdate() {
        return List.of(
                new MovieCreditPersonResponse(
                        PERSON_ID_1,
                        "Leonardo DiCaprio",
                        "leo.jpg",
                        Set.of(Profession.ACTOR)),
                new MovieCreditPersonResponse(
                        PERSON_ID_2,
                        "Christopher Nolan",
                        "nolan.jpg",
                        Set.of(Profession.ACTOR)));
    }

    /*
     * =====================================================
     * UPDATED STATE (DB AFTER UPDATE)
     * =====================================================
     */

    public static Movie updatedMovie_afterUpdate() {
        Movie movie = existingMovie_beforeUpdate();

        movie.setSynopsis("Updated synopsis");
        movie.setLanguages(List.of(Language.ENGLISH, Language.HINDI));
        movie.setReleaseDate(LocalDate.of(2010, 7, 25));

        return movie;
    }

    /*
     * =====================================================
     * RESPONSE STATE
     * =====================================================
     */

    public static MovieResponse updatedMovieResponse() {
        return new MovieResponse(
                MOVIE_ID,
                "Inception",
                "Inception",
                "Updated synopsis",
                List.of(Language.ENGLISH, Language.HINDI),
                List.of(Genre.SCI_FI),
                new ArrayList<>(),
                148,
                Rating.PG_13,
                LocalDate.of(2010, 7, 25),
                "poster.jpg",
                "trailer.mp4",
                true);
    }

}
