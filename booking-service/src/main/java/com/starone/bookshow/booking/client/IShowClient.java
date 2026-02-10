package com.starone.bookshow.booking.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.starone.springcommon.response.record.ShowResponse;

@FeignClient(name = "show-service", url = "${show.service.url:http://localhost:8084}")
public interface IShowClient {
    /**
     * Get full show details with enrichment (used in booking flow)
     */
    @GetMapping("/api/v1/shows/{showId}")
    ShowResponse getShowById(@PathVariable("showId") UUID showId);
}
