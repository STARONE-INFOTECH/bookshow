package com.starone.bookshow.movie.entity;

import java.util.List;
import java.util.UUID;

import com.starone.bookshow.movie.enums.EMovieGenre;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
