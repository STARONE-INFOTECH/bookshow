package com.starone.bookshow.movie.controller;

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

import com.starone.bookshow.movie.dto.MovieRequestDto;
import com.starone.bookshow.movie.service.IMovieService;
import com.starone.common.request.ApiResponses;
import com.starone.common.response.record.ApiResponse;
import com.starone.common.response.record.MovieResponse;
import com.starone.common.response.record.MovieShowResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {
    private final IMovieService movieService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MovieResponse> create(@Valid @RequestBody MovieRequestDto requestDto) {
        MovieResponse response = movieService.create(requestDto);
        return ApiResponses.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<MovieResponse> getById(@PathVariable("id") UUID id) {
        MovieResponse response = movieService.getById(id);
        return ApiResponses.success(response);
    }

    @PatchMapping("/{id}")
    public ApiResponse<MovieResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody MovieRequestDto requestDto) {
        MovieResponse response = movieService.update(id, requestDto);
        return ApiResponses.success(response);
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<MovieResponse> deactivate(@PathVariable("id") UUID id) {
        MovieResponse response = movieService.deactivate(id);
        return ApiResponses.success(response);
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<MovieResponse> activate(@PathVariable("id") UUID id) {
        MovieResponse response = movieService.activate(id);
        return ApiResponses.success(response);
    }

    @GetMapping("/now-showing")
    public ApiResponse<Page<MovieResponse>> getNowShowing(
            @PageableDefault(size = 20, sort = "releaseDate") Pageable pageable) {
        Page<MovieResponse> page = movieService.getNowShowing(pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/upcoming")
    public ApiResponse<Page<MovieResponse>> getUpcoming(
            @PageableDefault(size = 20, sort = "releaseDate") Pageable pageable) {
        Page<MovieResponse> page = movieService.getUpcoming(pageable);
        return ApiResponses.success(page);
    }

    @GetMapping
    public ApiResponse<Page<MovieResponse>> getAll(
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        Page<MovieResponse> page = movieService.getAll(pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/search")
    public ApiResponse<Page<MovieResponse>> searchByTitle(
            @RequestParam String title,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        Page<MovieResponse> page = movieService.searchByTitle(title, pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/genre/{genre}")
    public ApiResponse<Page<MovieResponse>> filterByGenre(
            @PathVariable("genre") String genre,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponse> page = movieService.filterByGenre(genre, pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/language/{language}")
    public ApiResponse<Page<MovieResponse>> filterByLanguage(
            @PathVariable("language") String language,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponse> page = movieService.filterByLanguage(language, pageable);
        return ApiResponses.success(page);
    }

    /*
     * ======================================================================
     * - Internal Service-to-Service usable endpoints by using Feign client -
     * ======================================================================
     */
    @GetMapping("/show/{movieId}")
    public ApiResponse<MovieShowResponse> getMovieById(@PathVariable("movieId") UUID movieId){
        return ApiResponses.success(movieService.getByMovieId(movieId));
    }

}
