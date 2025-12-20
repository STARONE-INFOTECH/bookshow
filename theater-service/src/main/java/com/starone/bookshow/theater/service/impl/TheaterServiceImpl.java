package com.starone.bookshow.theater.service.impl;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.theater.dto.TheaterRequestDto;
import com.starone.bookshow.theater.entity.Theater;
import com.starone.bookshow.theater.mapper.IScreenMapper;
import com.starone.bookshow.theater.mapper.ITheaterMapper;
import com.starone.bookshow.theater.repository.ITheaterRepository;
import com.starone.bookshow.theater.service.ITheaterService;
import com.starone.common.dto.TheaterResponseDto;
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TheaterServiceImpl implements ITheaterService {

    private final ITheaterRepository theaterRepository;
    private final ITheaterMapper theaterMapper;
    private final IScreenMapper screenMapper;

    @Override
    public TheaterResponseDto create(TheaterRequestDto requestDto) {
        if (theaterRepository.existsByNameAndCityIgnoreCase(requestDto.getName(), requestDto.getCity())) {
            throw new ConflictException(ErrorCodes.CONFLICT, 
                    "Theater with this name already exists in the city");
        }

        Theater theater = theaterMapper.toEntity(requestDto);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    @Transactional(readOnly = true)
    public TheaterResponseDto getById(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        return enrichResponse(theater);
    }

    @Override
    public TheaterResponseDto update(UUID id, TheaterRequestDto requestDto) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));

        // Check uniqueness if name or city changing
        if ((requestDto.getName() != null && !requestDto.getName().equalsIgnoreCase(theater.getName())) ||
            (requestDto.getCity() != null && !requestDto.getCity().equalsIgnoreCase(theater.getCity()))) {

            if (theaterRepository.existsByNameAndCityIgnoreCase(requestDto.getName(), requestDto.getCity())) {
                throw new ConflictException(ErrorCodes.CONFLICT, 
                        "Theater with this name already exists in the city");
            }
        }

        theaterMapper.updateEntity(requestDto, theater);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    public TheaterResponseDto deactivate(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        theater.setActive(false);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    public TheaterResponseDto activate(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        theater.setActive(true);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterResponseDto> getAllActive(Pageable pageable) {
        Page<Theater> page = theaterRepository.findByActiveTrue(pageable);
        return page.map(this::enrichResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterResponseDto> getByCity(String city, Pageable pageable) {
        Page<Theater> page = theaterRepository.findByCityIgnoreCase(city, pageable);
        return page.map(this::enrichResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterResponseDto> getByCityAndActive(String city, Pageable pageable) {
        Page<Theater> page = theaterRepository.findByCityIgnoreCaseAndActiveTrue(city, pageable);
        return page.map(this::enrichResponse);
    }

    private TheaterResponseDto enrichResponse(Theater theater) {
        TheaterResponseDto dto = theaterMapper.toResponseDto(theater);
        dto.setScreens(theater.getScreens().stream()
                .map(screenMapper::toResponseDto)
                .collect(Collectors.toSet()));
        return dto;
    }
}