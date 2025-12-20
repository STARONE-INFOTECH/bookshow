package com.starone.bookshow.theater.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.common.dto.ScreenResponseDto;

public interface IScreenService {
    
    ScreenResponseDto createScreen(UUID theaterId, ScreenRequestDto requestDto);

    ScreenResponseDto getScreenById(UUID screenId);

    ScreenResponseDto updateScreen(UUID screenId, ScreenRequestDto requestDto);

    ScreenResponseDto deactivateScreen(UUID screenId);

    ScreenResponseDto activateScreen(UUID screenId);

    Page<ScreenResponseDto> getScreensByTheaterId(UUID theaterId, Pageable pageable);

    Page<ScreenResponseDto> getActiveScreensByTheaterId(UUID theaterId, Pageable pageable);
}
