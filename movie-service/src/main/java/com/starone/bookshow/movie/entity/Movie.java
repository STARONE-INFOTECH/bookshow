package com.starone.bookshow.movie.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Rating;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String originalTitle;

    @Column(length = 2000)
    private String synopsis;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_languages", joinColumns = @JoinColumn(name = "movie_id"))
    @Enumerated(EnumType.STRING)
    private List<Language> languages;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Enumerated(EnumType.STRING)
    private List<Genre> genres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieCredit> movieCredits = new ArrayList<>();

    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private Rating rating;

    private LocalDate releaseDate;

    private String posterUrl;
    private String trailerUrl;

    @Column(nullable = false)
    private Boolean active = true;

    // private Integer totalReviews;

}
