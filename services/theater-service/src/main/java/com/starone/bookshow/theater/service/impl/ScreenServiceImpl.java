package com.starone.bookshow.theater.service.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.bookshow.theater.entity.Screen;
import com.starone.bookshow.theater.entity.Theater;
import com.starone.bookshow.theater.exception.TheaterException;
import com.starone.bookshow.theater.mapper.IScreenMapper;
import com.starone.bookshow.theater.projection.TheaterScreenShowProjection;
import com.starone.bookshow.theater.repository.IScreenRepository;
import com.starone.bookshow.theater.repository.ITheaterRepository;
import com.starone.bookshow.theater.service.IScreenService;
import com.starone.springcommon.exceptions.errorcodes.ErrorCode;
import com.starone.springcommon.response.record.ScreenResponse;
import com.starone.springcommon.response.record.TheaterScreenShowResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreenServiceImpl implements IScreenService {

    private static final Logger log = LoggerFactory.getLogger(ScreenServiceImpl.class);

    private final IScreenRepository screenRepository;
    private final ITheaterRepository theaterRepository;
    private final IScreenMapper screenMapper;

    @Override
    public ScreenResponse createScreen(UUID theaterId, ScreenRequestDto requestDto) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new TheaterException(ErrorCode.THEATER_NOT_FOUND, "Theater not found"));

        // Optional validation: parse JSON and verify totalSeats matches
        validateSeatLayout(requestDto.getSeatLayoutJson(), requestDto.getTotalSeats());

        Screen screen = new Screen();
        screen.setTheater(theater);
        screen.setName(requestDto.getName());
        screen.setFacilities(requestDto.getFacilities());
        screen.setSeatLayoutJson(requestDto.getSeatLayoutJson());
        screen.setTotalSeats(requestDto.getTotalSeats());

        screen = screenRepository.save(screen);
        return screenMapper.toResponseDto(screen);
    }

    @Override
    @Transactional(readOnly = true)
    public ScreenResponse getScreenById(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new TheaterException(ErrorCode.SCREEN_NOT_FOUND, "Screen not found"));
        return screenMapper.toResponseDto(screen);
    }

    @Override
    public ScreenResponse updateScreen(UUID screenId, ScreenRequestDto requestDto) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new TheaterException(ErrorCode.SCREEN_NOT_FOUND, "Screen not found"));

        if (requestDto.getSeatLayoutJson() != null) {
            validateSeatLayout(requestDto.getSeatLayoutJson(), requestDto.getTotalSeats());
        }

        if (requestDto.getName() != null)
            screen.setName(requestDto.getName());
        if (requestDto.getFacilities() != null)
            screen.setFacilities(requestDto.getFacilities());
        if (requestDto.getSeatLayoutJson() != null)
            screen.setSeatLayoutJson(requestDto.getSeatLayoutJson());
        if (requestDto.getTotalSeats() > 0)
            screen.setTotalSeats(requestDto.getTotalSeats());

        screen = screenRepository.save(screen);
        return screenMapper.toResponseDto(screen);
    }

    @Override
    public ScreenResponse deactivateScreen(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new TheaterException(ErrorCode.SCREEN_NOT_FOUND));
        screen.setActive(false);
        screen = screenRepository.save(screen);
        return screenMapper.toResponseDto(screen);
    }

    @Override
    public ScreenResponse activateScreen(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new TheaterException(ErrorCode.SCREEN_NOT_FOUND));
        screen.setActive(true);
        screen = screenRepository.save(screen);
        return screenMapper.toResponseDto(screen);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreenResponse> getScreensByTheaterId(UUID theaterId, Pageable pageable) {
        // First validate theater exists
        theaterRepository.findById(theaterId)
                .orElseThrow(() -> new TheaterException(ErrorCode.THEATER_NOT_FOUND));

        Page<Screen> page = screenRepository.findByTheaterId(theaterId, pageable);
        return page.map(screenMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreenResponse> getActiveScreensByTheaterId(UUID theaterId, Pageable pageable) {
        theaterRepository.findById(theaterId)
                .orElseThrow(() -> new TheaterException(
                        ErrorCode.SCREEN_NOT_FOUND, "Screen not found with theater Id :" + theaterId));

        Page<Screen> page = screenRepository.findByTheaterIdAndActiveTrue(theaterId, pageable);
        return page.map(screenMapper::toResponseDto);
    }

    /*
     * ====================================================================
     * --- Internal Service-To-Service usable methods with Feign client ---
     * ====================================================================
     */

    @Override
    public TheaterScreenShowResponse getTheaterByScreenId(UUID theaterId, UUID screenId) {
        TheaterScreenShowProjection screenTheaterProjection = screenRepository
                .findTheaterAndScreenByScreenId(theaterId, screenId)
                .orElseThrow(() -> {
                    log.warn("Theater not found with screen id: {}", screenId);
                    return new TheaterException(
                            ErrorCode.THEATER_NOT_FOUND,
                            "Theater not found with screen id+" + screenId);
                });
        log.info("Theater fetched successfully :[{}, {}, {}, {}, {}]",
                screenTheaterProjection.getTheaterId(),
                screenTheaterProjection.getTheaterName(),
                screenTheaterProjection.getCity(),
                screenTheaterProjection.getScreenId(),
                screenTheaterProjection.getScreenName());

        return new TheaterScreenShowResponse(
                screenTheaterProjection.getTheaterId(),
                screenTheaterProjection.getTheaterName(),
                screenTheaterProjection.getCity(),
                screenTheaterProjection.getScreenId(),
                screenTheaterProjection.getScreenName());
    }
    /*
     * =====================================================================
     * ------------------ Helper to enrich with screen --------------------
     * =====================================================================
     */

    // Simple validation - can be expanded with JSON schema or custom parser
    private void validateSeatLayout(String json, int totalSeats) {
        if (json == null || json.trim().isEmpty()) {
            throw new TheaterException(ErrorCode.VALIDATION_422, "Seat layout JSON is required");
        }
        // Add more validation (parse JSON, count seats, etc.) if needed
        if (totalSeats <= 0) {
            throw new TheaterException(ErrorCode.VALIDATION_422, "Total seats must be positive");
        }
    }
}
