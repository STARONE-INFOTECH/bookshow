package com.starone.bookshow.theater.service.impl;

import java.util.List;
import java.util.Set;
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
import com.starone.common.error.ErrorCodes;
import com.starone.common.exceptions.BadRequestException;
import com.starone.common.exceptions.ConflictException;
import com.starone.common.exceptions.NotFoundException;
import com.starone.common.response.record.ScreenResponse;
import com.starone.common.response.record.TheaterResponse;
import com.starone.common.response.record.TheaterScreenShowResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TheaterServiceImpl implements ITheaterService {

    private final ITheaterRepository theaterRepository;
    private final ITheaterMapper theaterMapper;
    private final IScreenMapper screenMapper;

    @Override
    public TheaterResponse create(TheaterRequestDto requestDto) {
        if (requestDto == null) {
            throw new BadRequestException(ErrorCodes.BAD_REQUEST, "Theater dto can not be null.");
        }
        if (requestDto.getName() == null || requestDto.getName().isEmpty() || requestDto.getCity() == null
                || requestDto.getCity().isEmpty()) {
            throw new BadRequestException(ErrorCodes.BAD_REQUEST, "Theater name and city can not be null or empty.");
        }
        if (theaterRepository.existsByNameAndCityIgnoreCase(requestDto.getName(), requestDto.getCity())) {
            throw new ConflictException(ErrorCodes.THEATER_ALREADY_EXISTS,
                    "Theater with this name already exists in the city");
        }

        Theater theater = theaterMapper.toEntity(requestDto);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    @Transactional(readOnly = true)
    public TheaterResponse getById(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        return enrichResponse(theater);
    }

    @Override
    public TheaterResponse update(UUID id, TheaterRequestDto requestDto) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));

        // Check uniqueness if name or city changing
        if ((requestDto.getName() != null && !requestDto.getName().equalsIgnoreCase(theater.getName())) ||
                (requestDto.getCity() != null && !requestDto.getCity().equalsIgnoreCase(theater.getCity()))) {

            if (theaterRepository.existsByNameAndCityIgnoreCase(requestDto.getName(), requestDto.getCity())) {
                throw new ConflictException(ErrorCodes.THEATER_ALREADY_EXISTS,
                        "Theater with this name already exists in the city");
            }
        }

        theaterMapper.updateEntity(requestDto, theater);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    public TheaterResponse deactivate(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        theater.setActive(false);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    public TheaterResponse activate(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOT_FOUND));
        theater.setActive(true);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterResponse> getAllActive(Pageable pageable) {
        Page<Theater> page = theaterRepository.findByActiveTrue(pageable);
        return page.map(this::enrichResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterResponse> getByCity(String city, Pageable pageable) {
        Page<Theater> page = theaterRepository.findByCityIgnoreCase(city, pageable);
        return page.map(this::enrichResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterResponse> getByCityAndActive(String city, Pageable pageable) {
        Page<Theater> page = theaterRepository.findByCityIgnoreCaseAndActiveTrue(city, pageable);
        return page.map(this::enrichResponse);
    }
    
     /*
     * =====================================================================
     * ------------------ Helper to enrich with theater --------------------
     * =====================================================================
     */

    private TheaterResponse enrichResponse(Theater theater) {
        Set<ScreenResponse> screens = theater.getScreens().stream()
                .map(screenMapper::toResponseDto)
                .collect(Collectors.toSet());
        return new TheaterResponse(
                theater.getId(),
                theater.getName(),
                theater.getDescription(),
                theater.getCity(),
                theater.getAddress(),
                theater.getLandmark(),
                theater.getLatitude(),
                theater.getLongitude(),
                theater.getContactPhone(),
                theater.getContactEmail(),
                theater.getAmenities(),
                theater.isActive(),
                theater.getTenantId(),
                screens);
    }

    
}