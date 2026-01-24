package com.starone.bookshow.theater.dto;

import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TheaterRequestDto {
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
    private UUID tenantId;  // optional partner link
}
