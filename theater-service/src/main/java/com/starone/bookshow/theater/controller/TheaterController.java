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
import com.starone.common.request.ApiResponses;
import com.starone.common.response.record.ApiResponse;
import com.starone.common.response.record.TheaterResponse;
import com.starone.common.response.record.TheaterScreenShowResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/theaters")
@RequiredArgsConstructor
public class TheaterController {
    private final ITheaterService theaterService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TheaterResponse> create(@Valid @RequestBody TheaterRequestDto requestDto) {
        TheaterResponse response = theaterService.create(requestDto);
        return ApiResponses.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<TheaterResponse> getById(@PathVariable UUID id) {
        TheaterResponse response = theaterService.getById(id);
        return ApiResponses.success(response);
    }

    @PatchMapping("/{id}")
    public ApiResponse<TheaterResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TheaterRequestDto requestDto) {
        TheaterResponse response = theaterService.update(id, requestDto);
        return ApiResponses.success(response);
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<TheaterResponse> deactivate(@PathVariable UUID id) {
        TheaterResponse response = theaterService.deactivate(id);
        return ApiResponses.success(response);
    }

    @PutMapping("/{id}/activate")
    public ApiResponse<TheaterResponse> activate(@PathVariable UUID id) {
        TheaterResponse response = theaterService.activate(id);
        return ApiResponses.success(response);
    }

    @GetMapping("/active")
    public ApiResponse<Page<TheaterResponse>> getAllActive(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<TheaterResponse> page = theaterService.getAllActive(pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/city/{city}")
    public ApiResponse<Page<TheaterResponse>> getByCity(
            @PathVariable String city,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<TheaterResponse> page = theaterService.getByCity(city, pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/city/{city}/active")
    public ApiResponse<Page<TheaterResponse>> getByCityAndActive(
            @PathVariable String city,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<TheaterResponse> page = theaterService.getByCityAndActive(city, pageable);
        return ApiResponses.success(page);
    }
     /*
     * =====================================================================
     * ------ Internal Service usable endpoints by using Feign client ------
     * =====================================================================
     */

}
