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

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.bookshow.theater.service.IScreenService;
import com.starone.common.request.ApiResponses;
import com.starone.common.response.record.ApiResponse;
import com.starone.common.response.record.ScreenResponse;
import com.starone.common.response.record.TheaterScreenShowResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/theaters/{theaterId}/screens")
@RequiredArgsConstructor
public class ScreenController {
    private final IScreenService screenService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScreenResponse> createScreen(
            @PathVariable UUID theaterId,
            @Valid @RequestBody ScreenRequestDto requestDto) {
        ScreenResponse response = screenService.createScreen(theaterId, requestDto);
        return ApiResponses.success(response);
    }

    @GetMapping("/{screenId}")
    public ApiResponse<ScreenResponse> getScreen(@PathVariable UUID screenId) {
        ScreenResponse response = screenService.getScreenById(screenId);
        return ApiResponses.success(response);
    }

    @PatchMapping("/{screenId}")
    public ApiResponse<ScreenResponse> updateScreen(
            @PathVariable UUID screenId,
            @Valid @RequestBody ScreenRequestDto requestDto) {
        ScreenResponse response = screenService.updateScreen(screenId, requestDto);
        return ApiResponses.success(response);
    }

    @PutMapping("/{screenId}/deactivate")
    public ApiResponse<ScreenResponse> deactivateScreen(@PathVariable UUID screenId) {
        ScreenResponse response = screenService.deactivateScreen(screenId);
        return ApiResponses.success(response);
    }

    @PutMapping("/{screenId}/activate")
    public ApiResponse<ScreenResponse> activateScreen(@PathVariable UUID screenId) {
        ScreenResponse response = screenService.activateScreen(screenId);
        return ApiResponses.success(response);
    }

    @GetMapping
    public ApiResponse<Page<ScreenResponse>> getScreens(
            @PathVariable UUID theaterId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ScreenResponse> page = screenService.getScreensByTheaterId(theaterId, pageable);
        return ApiResponses.success(page);
    }

    @GetMapping("/active")
    public ApiResponse<Page<ScreenResponse>> getActiveScreens(
            @PathVariable UUID theaterId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ScreenResponse> page = screenService.getActiveScreensByTheaterId(theaterId, pageable);
        return ApiResponses.success(page);
    }

    /*
     * ====================================================================
     * --- Internal Service-To-Service usable methods with Feign client ---
     * ====================================================================
     */
    @GetMapping("show/{screenId}")
    public ApiResponse<TheaterScreenShowResponse> getTheaterByScreenId(@PathVariable("screenId") UUID screenId) {
        return ApiResponses.success(screenService.getTheaterByScreenId(screenId));
    }
}
