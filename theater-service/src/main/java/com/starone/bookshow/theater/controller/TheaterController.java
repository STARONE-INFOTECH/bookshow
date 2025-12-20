package com.starone.bookshow.theater.controller;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.starone.bookshow.theater.dto.TheaterRequestDto;
import com.starone.bookshow.theater.service.ITheaterService;
import com.starone.common.dto.ApiResponse;
import com.starone.common.dto.TheaterResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/theaters")
@RequiredArgsConstructor
public class TheaterController {
    private final ITheaterService theaterService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TheaterResponseDto> create(@Valid @RequestBody TheaterRequestDto requestDto) {
        TheaterResponseDto response = theaterService.create(requestDto);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<TheaterResponseDto> getById(@PathVariable UUID id) {
        TheaterResponseDto response = theaterService.getById(id);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{id}")
    public ApiResponse<TheaterResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody TheaterRequestDto requestDto) {
        TheaterResponseDto response = theaterService.update(id, requestDto);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<TheaterResponseDto> deactivate(@PathVariable UUID id) {
        TheaterResponseDto response = theaterService.deactivate(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<TheaterResponseDto> activate(@PathVariable UUID id) {
        TheaterResponseDto response = theaterService.activate(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/active")
    public ApiResponse<Page<TheaterResponseDto>> getAllActive(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<TheaterResponseDto> page = theaterService.getAllActive(pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/city/{city}")
    public ApiResponse<Page<TheaterResponseDto>> getByCity(
            @PathVariable String city,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<TheaterResponseDto> page = theaterService.getByCity(city, pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/city/{city}/active")
    public ApiResponse<Page<TheaterResponseDto>> getByCityAndActive(
            @PathVariable String city,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<TheaterResponseDto> page = theaterService.getByCityAndActive(city, pageable);
        return ApiResponse.success(page);
    }
}
