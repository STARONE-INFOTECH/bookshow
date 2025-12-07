package com.starone.bookshow.movie.service;

import java.util.List;

import com.starone.bookshow.movie.dto.MovieDto;

public interface IMovieService {

    MovieDto addMovie(MovieDto movie);

    MovieDto updateMovie(String movieId, MovieDto movie);

    List<MovieDto> getAllMovies();

    MovieDto getMovieById(String movieId);

    void deleteMovieById(String movieId);
}
