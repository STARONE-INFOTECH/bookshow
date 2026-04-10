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
import com.starone.bookshow.movie.service.IMovieCreditService;
import com.starone.springcommon.response.record.ApiResponse;
import com.starone.springcommon.response.record.MovieCreditResponse;
import com.starone.springcommon.response.util.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/credits")
@RequiredArgsConstructor
public class MovieCreditController {
    private final IMovieCreditService movieCreditService;

    @GetMapping
    public ApiResponse<List<MovieCreditResponse>> getCredits(@PathVariable("movieId") UUID movieId) {
        List<MovieCreditResponse> credits = movieCreditService.getCreditsByMovieId(movieId);
        return ApiResponses.success(credits);
    }

    @GetMapping("/paginated")
    public ApiResponse<Page<MovieCreditResponse>> getCreditsPaginated(
            @PathVariable("movieId") UUID movieId,
            @PageableDefault(size = 50, sort = "billingOrder") Pageable pageable) {
        Page<MovieCreditResponse> page = movieCreditService.getCreditsByMovieIdPaginated(movieId, pageable);
        return ApiResponses.success(page);
    }

    @PutMapping("/reorder")
    public ApiResponse<List<MovieCreditResponse>> reorderCredits(
            @PathVariable("movieId") UUID movieId,
            @Valid @RequestBody List<MovieCreditRequestDto> orderedCredits) {
        List<MovieCreditResponse> reordered = movieCreditService.reorderCredits(movieId, orderedCredits);
        return ApiResponses.success(reordered);
    }
}
