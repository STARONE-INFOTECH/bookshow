package com.starone.bookshow.theater.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.bookshow.theater.service.IScreenService;
import com.starone.springcommon.response.record.ApiResponse;
import com.starone.springcommon.response.record.ScreenResponse;
import com.starone.springcommon.response.util.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final IScreenService screenService;

    /*
     * =====================
     * - SCREEN - ADMIN -
     * =====================
     */

    @GetMapping("/{screenId}")
    public ApiResponse<ScreenResponse> getScreenById(
            @PathVariable("screenId") UUID screenId) {
        ScreenResponse response = screenService.getScreenById(screenId);
        return ApiResponses.success(response);
    }

    @PatchMapping("/{screenId}")
    public ApiResponse<ScreenResponse> updateScreen(
            @PathVariable("screenId") UUID screenId,
            @Valid @RequestBody ScreenRequestDto requestDto) {
        ScreenResponse response = screenService.updateScreen(screenId, requestDto);
        return ApiResponses.success(response);
    }

    @PutMapping("/{screenId}/status")
    public ApiResponse<ScreenResponse> updateScreenStatus(
            @PathVariable UUID screenId,
            @RequestParam boolean active) {
        ScreenResponse response = active
                ? screenService.activateScreen(screenId)
                : screenService.deactivateScreen(screenId);
        return ApiResponses.success(response);
    }

    @GetMapping("/theater/{theaterId}")
    public ApiResponse<Page<ScreenResponse>> getScreensByTheaterId(
            @PathVariable UUID theaterId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ScreenResponse> page = screenService.getScreensByTheaterId(theaterId, pageable);
        return ApiResponses.success(page);
    }
    /*
     * ===================
     * - SCREEN - PUBLIC -
     * ===================
     */

}
