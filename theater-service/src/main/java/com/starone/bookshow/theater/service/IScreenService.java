package com.starone.bookshow.theater.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.common.response.record.ScreenResponse;
import com.starone.common.response.record.TheaterScreenShowResponse;

public interface IScreenService {

    ScreenResponse createScreen(UUID theaterId, ScreenRequestDto requestDto);

    ScreenResponse getScreenById(UUID screenId);

    ScreenResponse updateScreen(UUID screenId, ScreenRequestDto requestDto);

    ScreenResponse deactivateScreen(UUID screenId);

    ScreenResponse activateScreen(UUID screenId);

    Page<ScreenResponse> getScreensByTheaterId(UUID theaterId, Pageable pageable);

    Page<ScreenResponse> getActiveScreensByTheaterId(UUID theaterId, Pageable pageable);

     /*
     * ====================================================================
     * --- Internal Service-To-Service usable methods with Feign client ---
     * ====================================================================
     */
    TheaterScreenShowResponse getTheaterByScreenId(UUID screenId);
}
