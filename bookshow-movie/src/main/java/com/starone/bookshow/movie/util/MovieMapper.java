package com.starone.bookshow.movie.util;

import org.modelmapper.ModelMapper;

import com.starone.bookshow.movie.dto.CastAndCrewDto;
import com.starone.bookshow.movie.dto.MovieDto;
import com.starone.bookshow.movie.entity.CastAndCrew;
import com.starone.bookshow.movie.entity.Movie;

public class MovieMapper {

    private ModelMapper mapper;

    public MovieMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public Movie mapToMovie(MovieDto movieDto) {
        return mapper.map(movieDto, Movie.class);
    }

    public MovieDto mapToMovieDto(Movie movie) {
        return mapper.map(movie, MovieDto.class);
    }

    public void mapToExistingMovie(MovieDto movieDto, Movie existingMovie) {
        mapper.map(movieDto, existingMovie);
    }

    public CastAndCrew mapToCastAndCrew(CastAndCrewDto castAndCrewDto) {
        return mapper.map(castAndCrewDto, CastAndCrew.class);
    }

    public CastAndCrewDto mapToCastAndCrewDto(CastAndCrew castAndCrew) {
        return mapper.map(castAndCrew, CastAndCrewDto.class);
    }

    public void mapToExistingCastAndCrew(CastAndCrewDto castAndCrewDto, CastAndCrew existingCastAndCrew) {
        mapper.map(castAndCrewDto, existingCastAndCrew);
    }

    

}
