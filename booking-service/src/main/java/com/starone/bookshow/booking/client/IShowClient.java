package com.starone.bookshow.booking.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.starone.common.dto.ShowResponseDto;

@FeignClient(name = "show-service", url = "${show.service.url:http://localhost:8084}")
public interface IShowClient {
    /**
     * Get full show details with enrichment (used in booking flow)
     */
    @GetMapping("/api/v1/shows/{showId}")
    ShowResponseDto getShowById(@PathVariable("showId") UUID showId);

    // Optional: if you need more endpoints later
    // @GetMapping("/api/v1/shows/movie/{movieId}")
    // Page<ShowResponseDto> getShowsByMovieId(@PathVariable UUID movieId, Pageable pageable);
}
