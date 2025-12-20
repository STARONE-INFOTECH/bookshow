package com.starone.bookshow.booking.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.starone.common.dto.ShowSeatResponseDto;

@FeignClient(name = "show-service", url = "${show.service.url:http://localhost:8084}")
public interface IShowSeatClient {
    @PostMapping("/api/v1/shows/{showId}/seats/lock")
    List<ShowSeatResponseDto> lockSeats(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers,
            @RequestParam UUID userId);

    @PostMapping("/api/v1/shows/{showId}/seats/release")
    void releaseSeats(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers);

    @PostMapping("/api/v1/shows/{showId}/seats/book")
    void bookSeats(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers,
            @RequestParam UUID bookingId);

    @PostMapping("/api/v1/shows/{showId}/seats/check")
    boolean areSeatsAvailable(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers);
}
