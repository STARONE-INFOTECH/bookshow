package com.starone.bookshow.show.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.starone.common.dto.ScreenResponseDto;
import com.starone.common.dto.TheaterResponseDto;

@FeignClient(name = "theater-service", url = "${theater.service.url:http://localhost:8083}")
public interface TheaterClient {
    @GetMapping("/api/v1/theaters/{theaterId}/screens/{screenId}")
    ScreenResponseDto getScreenById(@PathVariable("screenId") UUID screenId);

    // Helper to get theater from screen (if no direct endpoint)
    @GetMapping("/api/v1/theaters/{id}")
    TheaterResponseDto getTheaterById(@PathVariable("id") UUID theaterId);

    // Optional: get theater by screenId if you add endpoint
    @GetMapping("/api/v1/theaters/screens/{screenId}/theater")
    TheaterResponseDto getTheaterByScreenId(@PathVariable UUID screenId);

}
