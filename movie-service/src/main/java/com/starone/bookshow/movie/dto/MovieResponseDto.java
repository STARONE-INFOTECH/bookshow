package com.starone.bookshow.movie.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.starone.common.dto.PersonResponseDto;
import com.starone.common.enums.Genre;
import com.starone.common.enums.Language;
import com.starone.common.enums.Rating;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovieResponseDto {
    private UUID id;
    private String title;
    private String originalTitle;
    private String synopsis;

    private List<Language> languages;
    private List<Genre> genres;
    private List<MovieCreditResponseDto> movieCredits;

    private Integer durationMinutes;
    private Rating rating;
    private LocalDate releaseDate;

    private String posterUrl;
    private String trailerUrl;

    private Boolean active;
}
