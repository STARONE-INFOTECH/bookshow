package com.starone.bookshow.show.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.starone.bookshow.show.dto.ShowRequestDto;
import com.starone.bookshow.show.service.IShowSeatService;
import com.starone.bookshow.show.service.IShowService;
import com.starone.springcommon.response.record.ApiResponse;
import com.starone.springcommon.response.record.ShowResponse;
import com.starone.springcommon.response.record.ShowSeatResponse;
import com.starone.springcommon.response.util.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
public class ShowController {

    private final IShowService showService;
    private final IShowSeatService showSeatService;

    /*
     * ======================================================================
     * - ADMIN ENDPOINTS -
     * ======================================================================
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShowResponse> createShow(@Valid @RequestBody ShowRequestDto requestDto) {
        ShowResponse response = showService.createShow(requestDto);
        return ApiResponses.success(response);
    }

    @PatchMapping("/{showId}")
    public ApiResponse<ShowResponse> updateShow(
            @PathVariable UUID showId,
            @Valid @RequestBody ShowRequestDto requestDto) {
        ShowResponse response = showService.updateShow(showId, requestDto);
        return ApiResponses.success(response);
    }

    @PutMapping("/{showId}/deactivate")
    public ApiResponse<Void> deactivateShow(@PathVariable UUID showId) {
        showService.deactivateShow(showId);
        return ApiResponses.success(null);
    }

    @PutMapping("/{showId}/activate")
    public ApiResponse<Void> activateShow(@PathVariable UUID showId) {
        showService.activateShow(showId);
        return ApiResponses.success(null);
    }

    /*
     * ======================================================================
     * - PUBLIC ENDPOINTS -
     * ======================================================================
     */
    @GetMapping("/{showId}")
    public ApiResponse<ShowResponse> getShowById(@PathVariable UUID showId) {
        ShowResponse response = showService.getShowById(showId);
        return ApiResponses.success(response);
    }

    @GetMapping("/movie/{movieId}")
    public ApiResponse<Page<ShowResponse>> getShowsByMovieId(
            @PathVariable UUID movieId,
            @PageableDefault(size = 20, sort = "showStartTime") Pageable pageable) {
        Page<ShowResponse> page = showService.getShowsByMovieId(movieId, pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/{theaterId}/screen/{screenId}/date")
    public ApiResponse<Page<ShowResponse>> getShowsByScreenAndDate(
            @PathVariable UUID theaterId,
            @PathVariable UUID screenId,
            @RequestParam LocalDateTime date,
            @PageableDefault(size = 20, sort = "showStartTime") Pageable pageable) {
        Page<ShowResponse> page = showService.getShowsByScreenAndDate(theaterId, screenId, date, pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/today")
    public ApiResponse<Page<ShowResponse>> getTodayShows(
            @PageableDefault(size = 20, sort = "showStartTime") Pageable pageable) {
        Page<ShowResponse> page = showService.getTodayShows(pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/upcoming")
    public ApiResponse<Page<ShowResponse>> getUpcomingShows(
            @PageableDefault(size = 20, sort = "showStartTime") Pageable pageable) {
        Page<ShowResponse> page = showService.getUpcomingShows(pageable);
        return ApiResponses.success(page);
    }

    /*
     * ======================================================================
     * - SEAT OPERATIONS -
     * ======================================================================
     */
    @PostMapping("/{showId}/seats/lock")
    public ApiResponse<List<ShowSeatResponse>> lockSeats(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers,
            @RequestParam UUID userId) {
        List<ShowSeatResponse> lockedSeats = showSeatService.lockSeats(showId, seatNumbers, userId);
        return ApiResponses.success(lockedSeats);
    }

    @PostMapping("/{showId}/seats/check")
    public ApiResponse<Boolean> checkSeatsAvailable(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers) {
        boolean available = showSeatService.areSeatsAvailable(showId, seatNumbers);
        return ApiResponses.success(available);
    }

    @GetMapping("/{showId}/seats")
    public ApiResponse<List<ShowSeatResponse>> getAllSeats(@PathVariable UUID showId) {
        List<ShowSeatResponse> seats = showSeatService.getAllSeatsForShow(showId);
        return ApiResponses.success(seats);
    }

    @GetMapping("/{showId}/seats/status")
    public ApiResponse<List<ShowSeatResponse>> getSeatStatus(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers) {
        List<ShowSeatResponse> status = showSeatService.getSeatStatus(showId, seatNumbers);
        return ApiResponses.success(status);
    }
}
