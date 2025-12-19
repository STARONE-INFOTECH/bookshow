package com.starone.bookshow.theater.dto;

import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TheaterResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String city;
    private String address;
    private String landmark;
    private double latitude;
    private double longitude;
    private String contactPhone;
    private String contactEmail;
    private Set<String> amenities;
    private boolean active;
    private UUID tenantId;

    private Set<ScreenResponseDto> screens;
}
