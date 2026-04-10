package com.starone.bookshow.show.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.starone.springcommon.config.feign.CommonFeignConfig;
import com.starone.springcommon.response.record.ScreenResponse;
import com.starone.springcommon.response.record.TheaterResponse;
import com.starone.springcommon.response.record.TheaterScreenShowResponse;

@FeignClient(
        name = "theater-service", 
        configuration = CommonFeignConfig.class,
        url = "${theater.service.url:http://localhost:8083}")
public interface TheaterClient {
    @GetMapping("/api/v1/theaters/{theaterId}/screens/{screenId}")
    ScreenResponse getScreenById(@PathVariable("theaterId") UUID theaterId,
            @PathVariable("screenId") UUID screenId);

    // Helper to get theater from screen (if no direct endpoint)
    @GetMapping("/api/v1/theaters/{id}")
    TheaterResponse getTheaterById(@PathVariable("id") UUID theaterId);

    // Optional: get theater by screenId if you add endpoint
    @GetMapping("/api/v1/theaters/{theaterId}/screens/show/{screenId}")
    TheaterScreenShowResponse getTheaterByScreenId(
            @PathVariable("theaterId") UUID theaterId,
            @PathVariable("screenId") UUID screenId);
}
