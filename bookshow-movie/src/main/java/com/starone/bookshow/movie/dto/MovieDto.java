package com.starone.bookshow.movie.dto;

import java.util.List;
import java.util.UUID;

import com.starone.bookshow.movie.entity.CastAndCrew;
import com.starone.bookshow.movie.enums.EMovieGenre;

public class MovieDto {

    private UUID movieId;
    private String title; 
    private String about;

    private double duration;
    private String language;
    private List<EMovieGenre> genres;
    private List<CastAndCrew> castandCrew;
    private List<String> trailer;
    private Boolean isActive;


   /* private LocalDate releaseDate;
    private String cirtificate;
    private String trailerUrl;
    private String posterUrl;
    private Double rating;
    private Integer totalReviews;
    private String description;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;*/

}
