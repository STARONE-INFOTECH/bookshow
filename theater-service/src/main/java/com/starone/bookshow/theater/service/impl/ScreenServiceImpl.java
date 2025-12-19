package com.starone.bookshow.theater.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.theater.dto.ScreenRequestDto;
import com.starone.bookshow.theater.dto.ScreenResponseDto;
import com.starone.bookshow.theater.entity.Screen;
import com.starone.bookshow.theater.entity.Theater;
import com.starone.bookshow.theater.mapper.IScreenMapper;
import com.starone.bookshow.theater.repository.IScreenRepository;
import com.starone.bookshow.theater.repository.ITheaterRepository;
import com.starone.bookshow.theater.service.IScreenService;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreenServiceImpl implements IScreenService {

    private final IScreenRepository screenRepository;
    private final ITheaterRepository theaterRepository;
    private final IScreenMapper screenMapper;

    @Override
    public ScreenResponseDto createScreen(UUID theaterId, ScreenRequestDto requestDto) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Theater not found"));

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
    public ScreenResponseDto getScreenById(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Screen not found"));
        return screenMapper.toResponseDto(screen);
    }

    @Override
    public ScreenResponseDto updateScreen(UUID screenId, ScreenRequestDto requestDto) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND, "Screen not found"));

        if (requestDto.getSeatLayoutJson() != null) {
            validateSeatLayout(requestDto.getSeatLayoutJson(), requestDto.getTotalSeats());
        }

        if (requestDto.getName() != null) screen.setName(requestDto.getName());
        if (requestDto.getFacilities() != null) screen.setFacilities(requestDto.getFacilities());
        if (requestDto.getSeatLayoutJson() != null) screen.setSeatLayoutJson(requestDto.getSeatLayoutJson());
        if (requestDto.getTotalSeats() > 0) screen.setTotalSeats(requestDto.getTotalSeats());

        screen = screenRepository.save(screen);
        return screenMapper.toResponseDto(screen);
    }

    @Override
    public ScreenResponseDto deactivateScreen(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        screen.setActive(false);
        screen = screenRepository.save(screen);
        return screenMapper.toResponseDto(screen);
    }

    @Override
    public ScreenResponseDto activateScreen(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        screen.setActive(true);
        screen = screenRepository.save(screen);
        return screenMapper.toResponseDto(screen);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreenResponseDto> getScreensByTheaterId(UUID theaterId, Pageable pageable) {
        // First validate theater exists
        theaterRepository.findById(theaterId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));

        Page<Screen> page = screenRepository.findByTheaterId(theaterId, pageable);
        return page.map(screenMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreenResponseDto> getActiveScreensByTheaterId(UUID theaterId, Pageable pageable) {
        theaterRepository.findById(theaterId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));

        Page<Screen> page = screenRepository.findByTheaterIdAndActiveTrue(theaterId, pageable);
        return page.map(screenMapper::toResponseDto);
    }

    // Simple validation - can be expanded with JSON schema or custom parser
    private void validateSeatLayout(String json, int totalSeats) {
        if (json == null || json.trim().isEmpty()) {
            throw new BadRequestException(ErrorCodes.BAD_REQUEST, "Seat layout JSON is required");
        }
        // Add more validation (parse JSON, count seats, etc.) if needed
        if (totalSeats <= 0) {
            throw new BadRequestException(ErrorCodes.BAD_REQUEST, "Total seats must be positive");
        }
    }
}
