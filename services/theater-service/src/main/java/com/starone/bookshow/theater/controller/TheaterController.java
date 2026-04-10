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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.bookshow.theater.dto.TheaterRequestDto;
import com.starone.bookshow.theater.service.IScreenService;
import com.starone.bookshow.theater.service.ITheaterService;
import com.starone.springcommon.response.record.ApiResponse;
import com.starone.springcommon.response.record.ScreenResponse;
import com.starone.springcommon.response.record.TheaterResponse;
import com.starone.springcommon.response.record.TheaterScreenShowResponse;
import com.starone.springcommon.response.util.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/theaters")
@RequiredArgsConstructor
public class TheaterController {
    private final ITheaterService theaterService;
    private final IScreenService screenService;

    /*
     * =============================
     * ----- THEATER : ADMIN -------
     * =============================
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TheaterResponse> createTheater(@Valid @RequestBody TheaterRequestDto requestDto) {
        TheaterResponse response = theaterService.create(requestDto);
        return ApiResponses.success(response);
    }

    @PatchMapping("/{theaterId}")
    public ApiResponse<TheaterResponse> updateTheater(
            @PathVariable("theaterId") UUID id,
            @Valid @RequestBody TheaterRequestDto requestDto) {
        TheaterResponse response = theaterService.update(id, requestDto);
        return ApiResponses.success(response);
    }

    @PutMapping("/{theaterId}/status")
    public ApiResponse<TheaterResponse> updateTheaterStatus(
            @PathVariable("theaterId") UUID id,
            @RequestParam boolean active) {
        TheaterResponse response = active
                ? theaterService.activate(id)
                : theaterService.deactivate(id);
        return ApiResponses.success(response);
    }

    /*
     * =============================
     * ----- THEATER : PUBLIC ------
     * =============================
     */
    @GetMapping("/{theaterId}")
    public ApiResponse<TheaterResponse> getTheaterById(@PathVariable("theaterId") UUID id) {
        TheaterResponse response = theaterService.getById(id);
        return ApiResponses.success(response);
    }

    @GetMapping("/search")
    public ApiResponse<Page<TheaterResponse>> searchTheaters(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ApiResponses.success(theaterService.search(city, active, pageable));
    }

    /*
     * =====================
     * - THEATER - SCREENS -
     * =====================
     */

    @PostMapping("/{theaterId}/screens")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScreenResponse> createScreen(
            @PathVariable UUID theaterId,
            @Valid @RequestBody ScreenRequestDto requestDto) {
        ScreenResponse response = screenService.createScreen(theaterId, requestDto);
        return ApiResponses.success(response);
    }

    @GetMapping("/{theaterId}/screens/show/{screenId}")
    public ApiResponse<TheaterScreenShowResponse> getTheaterByScreenId(
            @PathVariable("theaterId") UUID theaterId,
            @PathVariable("screenId") UUID screenId) {
        return ApiResponses.success(screenService.getTheaterByScreenId(theaterId, screenId));
    }

}
