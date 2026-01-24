package com.starone.bookshow.show.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.starone.common.response.record.ScreenResponse;
import com.starone.common.response.record.TheaterResponse;
import com.starone.common.response.record.TheaterScreenShowResponse;

@FeignClient(name = "theater-service", url = "${theater.service.url:http://localhost:8083}")
public interface TheaterClient {
    @GetMapping("/api/v1/theaters/{theaterId}/screens/{screenId}")
    ScreenResponse getScreenById(@PathVariable("screenId") UUID screenId);

    // Helper to get theater from screen (if no direct endpoint)
    @GetMapping("/api/v1/theaters/{id}")
    TheaterResponse getTheaterById(@PathVariable("id") UUID theaterId);

    // Optional: get theater by screenId if you add endpoint
    @GetMapping("/api/v1/theaters/{theaterId}/screens/show/{screenId}")
    TheaterScreenShowResponse getTheaterByScreenId(@PathVariable UUID screenId);

}
