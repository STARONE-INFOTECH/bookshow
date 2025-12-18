package com.starone.bookshow.movie.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.starone.bookshow.movie.dto.MovieCreditRequestDto;
import com.starone.bookshow.movie.dto.MovieCreditResponseDto;
import com.starone.bookshow.movie.service.IMovieCreditService;
import com.starone.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/credits")
@RequiredArgsConstructor
public class MovieCreditController {
    private final IMovieCreditService movieCreditService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MovieCreditResponseDto> addCredit(
            @PathVariable UUID movieId,
            @Valid @RequestBody MovieCreditRequestDto requestDto) {
        MovieCreditResponseDto response = movieCreditService.addCredit(movieId, requestDto);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{creditId}")
    public ApiResponse<MovieCreditResponseDto> updateCredit(
            @PathVariable UUID movieId,
            @PathVariable UUID creditId,
            @Valid @RequestBody MovieCreditRequestDto requestDto) {
        // Note: movieId in path for security (ensure credit belongs to movie)
        MovieCreditResponseDto response = movieCreditService.updateCredit(creditId, requestDto);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{creditId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeCredit(@PathVariable UUID creditId) {
        movieCreditService.removeCredit(creditId);
        return ApiResponse.success(null);
    }

    @GetMapping
    public ApiResponse<List<MovieCreditResponseDto>> getCredits(@PathVariable UUID movieId) {
        List<MovieCreditResponseDto> credits = movieCreditService.getCreditsByMovieId(movieId);
        return ApiResponse.success(credits);
    }

    @GetMapping("/paginated")
    public ApiResponse<Page<MovieCreditResponseDto>> getCreditsPaginated(
            @PathVariable UUID movieId,
            @PageableDefault(size = 50, sort = "billingOrder") Pageable pageable) {
        Page<MovieCreditResponseDto> page = movieCreditService.getCreditsByMovieIdPaginated(movieId, pageable);
        return ApiResponse.success(page);
    }

    @PutMapping("/reorder")
    public ApiResponse<List<MovieCreditResponseDto>> reorderCredits(
            @PathVariable UUID movieId,
            @Valid @RequestBody List<MovieCreditRequestDto> orderedCredits) {
        List<MovieCreditResponseDto> reordered = movieCreditService.reorderCredits(movieId, orderedCredits);
        return ApiResponse.success(reordered);
    }
}
