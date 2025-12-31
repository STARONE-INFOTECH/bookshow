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
import com.starone.common.dto.ApiResponse;
import com.starone.common.dto.MovieResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {
    private final IMovieService movieService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MovieResponseDto> create(@Valid @RequestBody MovieRequestDto requestDto) {
        MovieResponseDto response = movieService.create(requestDto);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<MovieResponseDto> getById(@PathVariable("id") UUID id) {
        MovieResponseDto response = movieService.getById(id);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{id}")
    public ApiResponse<MovieResponseDto> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody MovieRequestDto requestDto) {
        MovieResponseDto response = movieService.update(id, requestDto);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<MovieResponseDto> deactivate(@PathVariable("id") UUID id) {
        MovieResponseDto response = movieService.deactivate(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<MovieResponseDto> activate(@PathVariable("id") UUID id) {
        MovieResponseDto response = movieService.activate(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/now-showing")
    public ApiResponse<Page<MovieResponseDto>> getNowShowing(
            @PageableDefault(size = 20, sort = "releaseDate") Pageable pageable) {
        Page<MovieResponseDto> page = movieService.getNowShowing(pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/upcoming")
    public ApiResponse<Page<MovieResponseDto>> getUpcoming(
            @PageableDefault(size = 20, sort = "releaseDate") Pageable pageable) {
        Page<MovieResponseDto> page = movieService.getUpcoming(pageable);
        return ApiResponse.success(page);
    }

    @GetMapping
    public ApiResponse<Page<MovieResponseDto>> getAll(
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        Page<MovieResponseDto> page = movieService.getAll(pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/search")
    public ApiResponse<Page<MovieResponseDto>> searchByTitle(
            @RequestParam String title,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        Page<MovieResponseDto> page = movieService.searchByTitle(title, pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/genre/{genre}")
    public ApiResponse<Page<MovieResponseDto>> filterByGenre(
            @PathVariable("genre") String genre,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponseDto> page = movieService.filterByGenre(genre, pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/language/{language}")
    public ApiResponse<Page<MovieResponseDto>> filterByLanguage(
            @PathVariable("language") String language,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponseDto> page = movieService.filterByLanguage(language, pageable);
        return ApiResponse.success(page);
    }
}
