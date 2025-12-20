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
import com.starone.bookshow.show.dto.ShowResponseDto;
import com.starone.bookshow.show.dto.ShowSeatResponseDto;
import com.starone.bookshow.show.service.IShowSeatService;
import com.starone.bookshow.show.service.IShowService;
import com.starone.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
public class ShowController {
    private final IShowService showService;
    private final IShowSeatService showSeatService;

    // ====================== ADMIN ENDPOINTS ======================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShowResponseDto> createShow(@Valid @RequestBody ShowRequestDto requestDto) {
        ShowResponseDto response = showService.createShow(requestDto);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{showId}")
    public ApiResponse<ShowResponseDto> updateShow(
            @PathVariable UUID showId,
            @Valid @RequestBody ShowRequestDto requestDto) {
        ShowResponseDto response = showService.updateShow(showId, requestDto);
        return ApiResponse.success(response);
    }

    @PutMapping("/{showId}/deactivate")
    public ApiResponse<Void> deactivateShow(@PathVariable UUID showId) {
        showService.deactivateShow(showId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{showId}/activate")
    public ApiResponse<Void> activateShow(@PathVariable UUID showId) {
        showService.activateShow(showId);
        return ApiResponse.success(null);
    }

    // ====================== PUBLIC ENDPOINTS ======================

    @GetMapping("/{showId}")
    public ApiResponse<ShowResponseDto> getShowById(@PathVariable UUID showId) {
        ShowResponseDto response = showService.getShowById(showId);
        return ApiResponse.success(response);
    }

    @GetMapping("/movie/{movieId}")
    public ApiResponse<Page<ShowResponseDto>> getShowsByMovieId(
            @PathVariable UUID movieId,
            @PageableDefault(size = 20, sort = "showStartTime") Pageable pageable) {
        Page<ShowResponseDto> page = showService.getShowsByMovieId(movieId, pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/screen/{screenId}/date")
    public ApiResponse<Page<ShowResponseDto>> getShowsByScreenAndDate(
            @PathVariable UUID screenId,
            @RequestParam LocalDateTime date,
            @PageableDefault(size = 20, sort = "showStartTime") Pageable pageable) {
        Page<ShowResponseDto> page = showService.getShowsByScreenAndDate(screenId, date, pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/today")
    public ApiResponse<Page<ShowResponseDto>> getTodayShows(
            @PageableDefault(size = 20, sort = "showStartTime") Pageable pageable) {
        Page<ShowResponseDto> page = showService.getTodayShows(pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/upcoming")
    public ApiResponse<Page<ShowResponseDto>> getUpcomingShows(
            @PageableDefault(size = 20, sort = "showStartTime") Pageable pageable) {
        Page<ShowResponseDto> page = showService.getUpcomingShows(pageable);
        return ApiResponse.success(page);
    }

    // ====================== SEAT OPERATIONS ======================

    @PostMapping("/{showId}/seats/lock")
    public ApiResponse<List<ShowSeatResponseDto>> lockSeats(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers,
            @RequestParam UUID userId) {
        List<ShowSeatResponseDto> lockedSeats = showSeatService.lockSeats(showId, seatNumbers, userId);
        return ApiResponse.success(lockedSeats);
    }

    @PostMapping("/{showId}/seats/check")
    public ApiResponse<Boolean> checkSeatsAvailable(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers) {
        boolean available = showSeatService.areSeatsAvailable(showId, seatNumbers);
        return ApiResponse.success(available);
    }

    @GetMapping("/{showId}/seats")
    public ApiResponse<List<ShowSeatResponseDto>> getAllSeats(@PathVariable UUID showId) {
        List<ShowSeatResponseDto> seats = showSeatService.getAllSeatsForShow(showId);
        return ApiResponse.success(seats);
    }

    @GetMapping("/{showId}/seats/status")
    public ApiResponse<List<ShowSeatResponseDto>> getSeatStatus(
            @PathVariable UUID showId,
            @RequestBody List<String> seatNumbers) {
        List<ShowSeatResponseDto> status = showSeatService.getSeatStatus(showId, seatNumbers);
        return ApiResponse.success(status);
    }
}
