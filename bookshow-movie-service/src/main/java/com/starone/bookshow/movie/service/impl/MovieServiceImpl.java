package com.starone.bookshow.movie.service.impl;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.starone.bookshow.movie.dto.MovieDto;
import com.starone.bookshow.movie.entity.Movie;
import com.starone.bookshow.movie.exception.custom.InvalidInputException;
import com.starone.bookshow.movie.exception.custom.ResourceNotFoundException;
import com.starone.bookshow.movie.repository.IMovieRepository;
import com.starone.bookshow.movie.service.IMovieService;
import com.starone.bookshow.movie.util.MovieMapper;
import com.starone.bookshow.movie.util.MovieUtils;

@Service("movieService")
public class MovieServiceImpl implements IMovieService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovieServiceImpl.class);
    private final IMovieRepository movieRepository;
    private final MovieMapper mapper;

    public MovieServiceImpl( IMovieRepository movieRepository, MovieMapper mapper) {
        this.movieRepository = movieRepository;
        this.mapper = mapper;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto) {
        if (MovieUtils.isNull(movieDto)) {
            LOGGER.warn("Failed to save Movie! Movie must not be null");
            throw new InvalidInputException("Movie");
        }
        Movie savedMovie = movieRepository.save(mapper.mapToMovie(movieDto));

        LOGGER.info("Movie saved successfully");
        return mapper.mapToMovieDto(savedMovie);
    }

    @Override
    public MovieDto updateMovie(String movieId, MovieDto movieDto) {
        if (MovieUtils.isNullOrEmpty(movieId) || MovieUtils.isNull(movieDto)) {
            LOGGER.warn("Failed to update Movie! MovieId or Movie must not be null");
            throw new InvalidInputException("Movie or movieId");
        }
        Movie existingMovie = movieRepository.findById(UUID.fromString(movieId)).orElseThrow(() -> {
            LOGGER.warn("Failed to get Movie! Movie not found.");
            return new ResourceNotFoundException("Movie", movieId);
        });
        LOGGER.debug("MovieDto mapping with existing Movie");
        mapper.mapToExistingMovie(movieDto, existingMovie);

        Movie updatedMovie = movieRepository.save(mapper.mapToMovie(movieDto));

        LOGGER.info("Movie updated successfully.");
        return mapper.mapToMovieDto(updatedMovie);
    }

    @Override
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(mapper::mapToMovieDto)
                .toList();
    }

    @Override
    public MovieDto getMovieById(String movieId) {
        if (MovieUtils.isNullOrEmpty(movieId)) {
            LOGGER.warn("Failed to get Movie! MovieId must not be null");
            throw new InvalidInputException("MovieId");
        }
        Movie existingMovie = movieRepository.findById(UUID.fromString(movieId)).orElseThrow(() -> {
            LOGGER.warn("Failed to get Movie! Movie not found.");
            return new ResourceNotFoundException("Movie", movieId);
        });
        LOGGER.info("Movie fetched successfully!");
        return mapper.mapToMovieDto(existingMovie);
    }

    @Override
    public void deleteMovieById(String movieId) {
        if (MovieUtils.isNullOrEmpty(movieId)) {
            LOGGER.warn("Failed to delete Movie! MovieId must not be null");
            throw new InvalidInputException("MovieId");
        }
        movieRepository.deleteById(UUID.fromString(movieId));
        LOGGER.info("Movie deleted successfully!");
    }

}
