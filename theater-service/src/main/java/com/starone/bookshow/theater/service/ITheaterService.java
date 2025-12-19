package com.starone.bookshow.theater.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.theater.dto.TheaterRequestDto;
import com.starone.bookshow.theater.dto.TheaterResponseDto;

public interface ITheaterService {
    TheaterResponseDto create(TheaterRequestDto requestDto);

    TheaterResponseDto getById(UUID id);

    TheaterResponseDto update(UUID id, TheaterRequestDto requestDto);

    TheaterResponseDto deactivate(UUID id);

    TheaterResponseDto activate(UUID id);

    Page<TheaterResponseDto> getAllActive(Pageable pageable);

    Page<TheaterResponseDto> getByCity(String city, Pageable pageable);

    Page<TheaterResponseDto> getByCityAndActive(String city, Pageable pageable);
}
