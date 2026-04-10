package com.starone.bookshow.theater.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.starone.bookshow.theater.dto.TheaterRequestDto;
import com.starone.springcommon.response.record.TheaterResponse;

public interface ITheaterService {
    
    TheaterResponse create(TheaterRequestDto requestDto);

    TheaterResponse update(UUID id, TheaterRequestDto requestDto);

    TheaterResponse deactivate(UUID id);

    TheaterResponse activate(UUID id);

    TheaterResponse getById(UUID id);

    Page<TheaterResponse> search(String city, boolean active, Pageable pageable);

}
