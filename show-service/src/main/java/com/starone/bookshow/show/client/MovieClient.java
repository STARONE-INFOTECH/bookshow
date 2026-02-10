package com.starone.bookshow.show.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.starone.springcommon.config.feign.CommonFeignConfig;
import com.starone.springcommon.response.record.MovieShowResponse;

@FeignClient(
    name = "movie-service",
    configuration = CommonFeignConfig.class,
    url = "${movie.service.url:http://localhost:8082}")
public interface MovieClient {
    @GetMapping("/api/v1/movies/show/{id}")
    MovieShowResponse getMovieById(@PathVariable("id") UUID id);

    // Optional: search or list if needed
    // @GetMapping("/api/v1/movies/search")
    // Page<MovieResponseDto> searchMovies(@RequestParam String title, Pageable pageable);
}
