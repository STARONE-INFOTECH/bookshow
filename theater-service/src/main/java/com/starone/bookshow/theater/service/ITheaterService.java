package com.starone.bookshow.theater.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.theater.dto.TheaterRequestDto;
import com.starone.common.response.record.TheaterResponse;
import com.starone.common.response.record.TheaterScreenShowResponse;

public interface ITheaterService {
    TheaterResponse create(TheaterRequestDto requestDto);

    TheaterResponse getById(UUID id);

    TheaterResponse update(UUID id, TheaterRequestDto requestDto);

    TheaterResponse deactivate(UUID id);

    TheaterResponse activate(UUID id);

    Page<TheaterResponse> getAllActive(Pageable pageable);

    Page<TheaterResponse> getByCity(String city, Pageable pageable);

    Page<TheaterResponse> getByCityAndActive(String city, Pageable pageable);
   
}
