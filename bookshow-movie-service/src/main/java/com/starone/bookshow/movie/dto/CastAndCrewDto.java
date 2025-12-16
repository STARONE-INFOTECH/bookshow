package com.starone.bookshow.movie.dto;

import java.util.UUID;

import com.starone.bookshow.movie.request.PersonRequest;

public class CastAndCrewDto {
    private UUID castId;
    private PersonRequest person;
    private String role;
}
