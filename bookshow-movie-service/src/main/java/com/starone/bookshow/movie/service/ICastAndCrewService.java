package com.starone.bookshow.movie.service;

import java.util.List;

import com.starone.bookshow.movie.dto.CastAndCrewDto;

public interface ICastAndCrewService {

    CastAndCrewDto addCast(CastAndCrewDto castDto);

    CastAndCrewDto updateCast(String castId, CastAndCrewDto castDto);

    List<CastAndCrewDto> getAllCast();

    CastAndCrewDto getCastById(String castId);

    void deleteCastById(String castId);
}
