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
import com.starone.bookshow.theater.dto.ScreenResponseDto;
import com.starone.bookshow.theater.service.IScreenService;
import com.starone.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/theaters/{theaterId}/screens")
@RequiredArgsConstructor
public class ScreenController {
    private final IScreenService screenService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScreenResponseDto> createScreen(
            @PathVariable UUID theaterId,
            @Valid @RequestBody ScreenRequestDto requestDto) {
        ScreenResponseDto response = screenService.createScreen(theaterId, requestDto);
        return ApiResponse.success(response);
    }

    @GetMapping("/{screenId}")
    public ApiResponse<ScreenResponseDto> getScreen(@PathVariable UUID screenId) {
        ScreenResponseDto response = screenService.getScreenById(screenId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{screenId}")
    public ApiResponse<ScreenResponseDto> updateScreen(
            @PathVariable UUID screenId,
            @Valid @RequestBody ScreenRequestDto requestDto) {
        ScreenResponseDto response = screenService.updateScreen(screenId, requestDto);
        return ApiResponse.success(response);
    }

    @PutMapping("/{screenId}/deactivate")
    public ApiResponse<ScreenResponseDto> deactivateScreen(@PathVariable UUID screenId) {
        ScreenResponseDto response = screenService.deactivateScreen(screenId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{screenId}/activate")
    public ApiResponse<ScreenResponseDto> activateScreen(@PathVariable UUID screenId) {
        ScreenResponseDto response = screenService.activateScreen(screenId);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<Page<ScreenResponseDto>> getScreens(
            @PathVariable UUID theaterId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ScreenResponseDto> page = screenService.getScreensByTheaterId(theaterId, pageable);
        return ApiResponse.success(page);
    }

    @GetMapping("/active")
    public ApiResponse<Page<ScreenResponseDto>> getActiveScreens(
            @PathVariable UUID theaterId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ScreenResponseDto> page = screenService.getActiveScreensByTheaterId(theaterId, pageable);
        return ApiResponse.success(page);
    }
}
