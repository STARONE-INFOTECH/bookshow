package com.starone.bookshow.show.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.starone.common.dto.MovieResponseDto;

@FeignClient(name = "movie-service",url = "${movie.service.url:http://localhost:8081}")
public interface IMovieClient {
    @GetMapping("/api/v1/movies/{id}")
    MovieResponseDto getMovieById(@PathVariable("id") UUID id);

    // Optional: search or list if needed
    // @GetMapping("/api/v1/movies/search")
    // Page<MovieResponseDto> searchMovies(@RequestParam String title, Pageable pageable);
}
