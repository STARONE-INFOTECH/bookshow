package com.starone.bookshow.theater.service.impl;

import static com.starone.bookshow.theater.repository.TheaterSpecifications.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.starone.bookshow.theater.dto.TheaterRequestDto;
import com.starone.bookshow.theater.entity.Theater;
import com.starone.bookshow.theater.exception.TheaterException;
import com.starone.bookshow.theater.mapper.IScreenMapper;
import com.starone.bookshow.theater.mapper.ITheaterMapper;
import com.starone.bookshow.theater.repository.ITheaterRepository;
import com.starone.bookshow.theater.service.ITheaterService;
import com.starone.springcommon.exceptions.errorcodes.ErrorCode;
import com.starone.springcommon.response.record.ScreenResponse;
import com.starone.springcommon.response.record.TheaterResponse;

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
        if (theaterRepository.existsByNameAndCityIgnoreCase(requestDto.getName(), requestDto.getCity())) {
            throw new TheaterException(ErrorCode.THEATER_ALREADY_EXISTS,
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
                .orElseThrow(() -> new TheaterException(ErrorCode.THEATER_NOT_FOUND));
        return enrichResponse(theater);
    }

    @Override
    public TheaterResponse update(UUID id, TheaterRequestDto requestDto) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new TheaterException(ErrorCode.THEATER_NOT_FOUND));

        // Check uniqueness if name or city changing
        if ((requestDto.getName() != null && !requestDto.getName().equalsIgnoreCase(theater.getName())) ||
                (requestDto.getCity() != null && !requestDto.getCity().equalsIgnoreCase(theater.getCity()))) {

            if (theaterRepository.existsByNameAndCityIgnoreCase(requestDto.getName(), requestDto.getCity())) {
                throw new TheaterException(ErrorCode.THEATER_ALREADY_EXISTS,
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
                .orElseThrow(() -> new TheaterException(ErrorCode.THEATER_NOT_FOUND));
        theater.setActive(false);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    public TheaterResponse activate(UUID id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new TheaterException(ErrorCode.THEATER_NOT_FOUND));
        theater.setActive(true);
        theater = theaterRepository.save(theater);
        return enrichResponse(theater);
    }

    @Override
    public Page<TheaterResponse> search(String city, boolean active, Pageable pageable) {
        Specification<Theater> spec = Specification.where(hasCity(city)).and(isActive(active));

        return theaterRepository.findAll(spec,pageable).map(theaterMapper::toResponseDto);
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